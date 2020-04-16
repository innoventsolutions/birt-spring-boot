/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;
import com.innoventsolutions.birt.service.SubmitJobService;
import com.innoventsolutions.birt.service.SubmitListService;
import com.innoventsolutions.birt.util.Util;

import lombok.extern.slf4j.Slf4j;

/**
 * Controls the Async functions used to run reports and get status or fetch
 * content
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Slf4j
@RestController
public class SubmitController {
	@Autowired
	SubmitListService submitList;
	
	@Autowired
	public SubmitController() {
		log.info("Create Job Controller");
	}

	@Autowired
	private SubmitJobService submitter;

	@GetMapping("/testSubmit")
	public ResponseEntity<SubmitResponse> getTestSubmit(@RequestBody(required = false) Integer numToRun,
			final HttpServletResponse httpResponse) {
		log.info("testSubmit ");

		if (numToRun == null)
			numToRun = 10;

		final SubmitResponse outerResponse = new SubmitResponse(new ExecuteRequest());

		final String rptDesign = "param_test.rptdesign";
		final int min = 1;
		final int max = 10;
		for (int i = 0; i < numToRun; i++) {

			final int delay = ((int) (Math.random() * (max - min))) + min;
			final String outputName = "Test_" + i + " d(" + delay + ")";
			String format = "PDF";
			if ((i % 2) == 0)
				format = "HTML";

			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("paramString", "Ginger");
			params.put("paramDate", "2010-09-09");
			params.put("paramBoolean", true);
			params.put("paramDecimal", (i * 1.9) * i);
			params.put("paramInteger", i);
			params.put("delay", delay);

			final ExecuteRequest request = ExecuteRequest.builder().designFile(rptDesign).outputName(outputName)
					.format(format).parameters(params).build();

			executeSubmitJob(request, httpResponse);

		}

		return new ResponseEntity<SubmitResponse>(outerResponse, HttpStatus.OK);

	}

	@PostMapping("/submitJob")
	public ResponseEntity<SubmitResponse> executeSubmitJob(@RequestBody final ExecuteRequest request,
			final HttpServletResponse httpResponse) {
		final SubmitResponse submitResponse = new SubmitResponse(request);
		final CompletableFuture<SubmitResponse> submission = submitter.executeRunThenRender(submitResponse);
		submitList.put(submitResponse.getJobid(), submission);
		return new ResponseEntity<SubmitResponse>(submitResponse, submitResponse.getHttpStatus());
	}

	@GetMapping("/getJobInfo")
	public ResponseEntity<CompletableFuture<SubmitResponse>> getJobInfo(@RequestParam final String jobId) {

		final CompletableFuture<SubmitResponse> job = submitList.get(jobId);
		if (job == null) {
			log.info("Failure to find job: " + jobId + " on job stack");
			return new ResponseEntity<CompletableFuture<SubmitResponse>>(HttpStatus.NOT_FOUND);
		}
		log.info("Get Job: " + jobId + " stat: " + getJobStatus(job));

		return new ResponseEntity<CompletableFuture<SubmitResponse>>(job, HttpStatus.OK);

	}

	@GetMapping("/waitForJob")
	public ResponseEntity<SubmitResponse> waitForJobInfo(@RequestParam final String jobId) {

		final CompletableFuture<SubmitResponse> job = submitList.get(jobId);
		if (job == null) {
			log.info("Failure to find job: " + jobId + " on job stack");
			return new ResponseEntity<SubmitResponse>(HttpStatus.NOT_FOUND);
		}
		log.info("/waitForJob: " + jobId + " stat: " + getJobStatus(job));
		try {
			final SubmitResponse response = job.get();
			return new ResponseEntity<SubmitResponse>(response, response.getHttpStatus());
		} catch (final Exception e) {
			log.info("Failure to complete job: " + jobId, e);
			return new ResponseEntity<SubmitResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// TODO change to be a url parameter
	@DeleteMapping("/deleteJob")
	public ResponseEntity<Boolean> deleteJob(@RequestParam final String jobId) throws IOException {
		final CompletableFuture<SubmitResponse> job = submitList.get(jobId);
		if (job == null) {
			log.info("Failure to find job: " + jobId + " on job stack");
			return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
		}
		log.info("/deleteJob: " + jobId + " stat: " + getJobStatus(job));
		final boolean cancelled = job.cancel(true);
		if (cancelled) {
			submitList.remove(jobId);
		}
		return new ResponseEntity<Boolean>(Boolean.valueOf(cancelled), HttpStatus.OK);
	}

	// TODO This is an empty method
	@GetMapping("/getJob")
	public ResponseEntity<Object> getJob(@RequestParam final String jobId) {

		return new ResponseEntity<Object>(null, HttpStatus.OK);
	}

	// TODO change to be a URL
	@GetMapping("/getReport")
	public ResponseEntity<Resource> getReport(@RequestParam final String jobId) {
		log.debug("getReport " + jobId);
		final CompletableFuture<SubmitResponse> jobFuture = submitList.get(jobId);
		if (jobFuture == null) {
			return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
		}
		try {
			log.info("JOBFUTURE CLASS: " + jobFuture.getClass().toString());
			final SubmitResponse submitResponse = jobFuture.get();
			if (StatusEnum.COMPLETE.equals(submitResponse.getStatus())) {
				final FileInputStream fis = submitter.getReport(submitResponse);
				final HttpHeaders headers = new HttpHeaders();
				headers.set("Content-Disposition", "attachment; filename=\"" + submitResponse.getOutFileName() + "\"");

				final InputStreamResource resource = new InputStreamResource(fis);

				final MediaType contentType = Util.getMediaType(submitResponse.getRequest().getFormat());
				return ResponseEntity.ok().headers(headers).contentType(contentType).body(resource);
			}

			if (StatusEnum.EXCEPTION.equals(submitResponse.getStatus())) {
				log.error("Broken again");

			}

		} catch (final Exception e) {
			log.error("Failed to get report", e);
			return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// TODO: Can we get here and not be processing?
		return new ResponseEntity<Resource>(HttpStatus.PROCESSING);

	}

	// TODO Is this the right place for this lookup? perhaps a Util method?
	private StatusEnum getJobStatus(final CompletableFuture<SubmitResponse> future) {
		if (future.isDone())
			return StatusEnum.COMPLETE;
		if (future.isCompletedExceptionally())
			return StatusEnum.EXCEPTION;
		if (future.isCancelled())
			return StatusEnum.CANCELLED;

		return StatusEnum.UNKNOWN;

	}

}
