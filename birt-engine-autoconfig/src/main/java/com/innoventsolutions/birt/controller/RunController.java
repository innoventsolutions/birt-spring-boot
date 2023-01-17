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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.error.ApiError;
import com.innoventsolutions.birt.exception.BirtStarterException;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.util.Util;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to perform synchronous run Requests. 
 *  The StreamingResponseBody will cause the actual report execution to be 
 *   performed in a separate thread, with the response streamed directly to the caller.
 *   
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Slf4j @RestController
public class RunController {

	@Autowired
	public RunController() {
		log.debug("Run Controller Init");
	}

	@Autowired
	private ReportRunService runner;

	@GetMapping("/testPDF")
	public ResponseEntity<StreamingResponseBody> getTestPDF(final HttpServletResponse response) {
		log.info("testPDF ");

		final ExecuteRequest request = ExecuteRequest.builder()
				.designFile("TEST")
				.outputName("Test_Report.pdf")
				.format("PDF")
				.build();
		return executeRunReport(request, response);
	}

	@GetMapping("/testHTML")
	public ResponseEntity<StreamingResponseBody> getTestHTML(final HttpServletResponse response) {
		log.info("testHTML ");

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramString", "Ginger");
		params.put("paramDate", "2010-09-09");
		params.put("paramBoolean", true);
		params.put("paramDecimal", 1111.3333);
		params.put("paramInteger", 98765);
		final ExecuteRequest request = ExecuteRequest.builder()
				.designFile("param_test.rptdesign")
				.outputName("Test_Parameter_Report.pdf")
				.format("HTML")
				.parameters(params)
				.build();

		return executeRunReport(request, response);
	}


	/*
	 * Using the StreamingResponseBody causes a thread to spawn off we are using the
	 * default TaskExecutor to spawn those threads. may want to figure out how to
	 * use a defined task-executor NOTE: adding @Async will cause this to use our
	 * BirtAsyncConfigurer, but that does not seem to be complatible with
	 * StreamingResponseBody
	 *
	 */
	@PostMapping(value = "/runReport",consumes = {"text/plain", "application/*"},produces = {"text/plain", "application/*"})
	public ResponseEntity<StreamingResponseBody> executeRunReport(@RequestBody final ExecuteRequest request,
			final HttpServletResponse response) {

		log.info("Run Report: " + Thread.currentThread());
		final StreamingResponseBody responseBody = out -> {

			log.info("Run Report Lambda: " + Thread.currentThread());
			try {
				runner.execute(request, response.getOutputStream());
			} catch (final BirtStarterException e) {
				try {
					if (request.getWrapError()) {
						// send JSON with error code
						response.setStatus(e.getHttpCode().value());
						response.setContentType("application/json");
						final ApiError apiError = new ApiError(e.getHttpCode(), e.getMessage());
						final ObjectMapper mapper = new ObjectMapper();
						mapper.writeValue(response.getOutputStream(), apiError);
						
						
					} else {
						response.sendError(e.getHttpCode().value(), e.getMessage());
					}
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
			}
		};

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + request.getOutputName() + "." + request.getFormat())
				.contentType(Util.getMediaType(request.getFormat())).body(responseBody);
	}

}
