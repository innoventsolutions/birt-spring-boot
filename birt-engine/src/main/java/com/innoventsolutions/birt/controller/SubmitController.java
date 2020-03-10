package com.innoventsolutions.birt.controller;

import java.io.FileInputStream;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.JobStatus;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;
import com.innoventsolutions.birt.service.SubmitJobService;
import com.innoventsolutions.birt.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SubmitController {

	@Autowired
	public SubmitController() {
		log.info("Create Job Controller");
	}

	@Autowired
	private SubmitJobService submitter;

	@GetMapping("/testSubmit")
	public ResponseEntity<SubmitResponse> getTestSubmit(@RequestBody(required = false) Integer numToRun, final HttpServletResponse httpResponse) {
		log.info("testSubmit ");

		if (numToRun == null)
			numToRun = 10;

		SubmitResponse outerResponse = new SubmitResponse(new ExecuteRequest());

		final String rptDesign = "param_test.rptdesign";
		int min = 1;
		int max = 10;
		for (int i = 0; i < numToRun; i++) {

			int delay = ((int) (Math.random() * (max - min))) + min;
			final String outputName = "Test_" + i + " d(" + delay + ")";
			String format = "PDF";
			if ((i % 2) / 2 == 0)
				format = "HTML";

			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("paramString", "Ginger");
			params.put("paramDate", "2010-09-09");
			params.put("paramBoolean", true);
			params.put("paramDecimal", (i * 1.9) * i);
			params.put("paramInteger", i);
			params.put("delay", delay);
			final ExecuteRequest request = new ExecuteRequest(rptDesign, outputName, format, params);

			executeSubmitJob(request, httpResponse);

		}

		return new ResponseEntity<SubmitResponse>(outerResponse, HttpStatus.OK);

	}

	private Map<String, CompletableFuture<SubmitResponse>> submitList = new HashMap<String, CompletableFuture<SubmitResponse>>();

	@GetMapping("/submitReport")
	public ResponseEntity<SubmitResponse> executeSubmitJob(@RequestBody final ExecuteRequest request, final HttpServletResponse httpResponse) {

		SubmitResponse submitResponse = new SubmitResponse(request);
		CompletableFuture<SubmitResponse> submission = submitter.executeRunThenRender(submitResponse, httpResponse);
		submitList.put(submitResponse.getJobid(), submission);

		return new ResponseEntity<SubmitResponse>(submitResponse, HttpStatus.OK);
	}

	@GetMapping("/getJobInfo")
	public ResponseEntity<CompletableFuture<SubmitResponse>> getJobInfo(@RequestBody final JobStatus jobStatus) {

		CompletableFuture<SubmitResponse> job = submitList.get(jobStatus.getJobid());
		if (job == null) {
			log.info("Failure to find job: " + jobStatus.getJobid() + " on job stack");
			return new ResponseEntity<CompletableFuture<SubmitResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		log.info("Get Job: " + jobStatus.getJobid() + " stat: " + getJobStatus(job));

		return new ResponseEntity<CompletableFuture<SubmitResponse>>(job, HttpStatus.OK);

	}

	@GetMapping("/getJob")
	public ResponseEntity<Object> getJob(@RequestBody final JobStatus jobStatus) {

		return new ResponseEntity<Object>(null, HttpStatus.OK);
	}

	@GetMapping("/getReport")
	public ResponseEntity<Resource> getReport(@RequestBody final JobStatus jobStatus) {
		log.debug("getReport " + jobStatus);
		CompletableFuture<SubmitResponse> jobFuture = submitList.get(jobStatus.getJobid());
		if (jobFuture == null) {
			return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (StatusEnum.COMPLETE != getJobStatus(jobFuture)) {
			return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			SubmitResponse submitReponse = jobFuture.get();
			FileInputStream fis = submitter.getReport(submitReponse);
			final HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Disposition", "attachment; filename=\"" + submitReponse.getOutFileName() + "\"");

			InputStreamResource resource = new InputStreamResource(fis);

			MediaType contentType = Util.getMediaType(submitReponse.getRequest().getFormat());
			return ResponseEntity.ok().headers(headers).contentType(contentType).body(resource);
		} catch (Exception e) {
			return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//TODO Is this the right place for this lookup? perhaps a Util method?
	private StatusEnum getJobStatus(CompletableFuture<SubmitResponse> future) {
		if (future.isDone())
			return StatusEnum.COMPLETE;
		if (future.isCompletedExceptionally())
			return StatusEnum.EXCEPTION;
		if (future.isCancelled())
			return StatusEnum.CANCELLED;

		return StatusEnum.UNKNOWN;

	}

}
