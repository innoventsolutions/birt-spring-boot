package com.innoventsolutions.birt.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BirtStarterException;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
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

		final String rptDesign = "TEST";
		final String humanName = "Test_Report.pdf";
		final String format = "PDF";
		final ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format, null);
		return executeRunReport(request, response);
	}

	@GetMapping("/testHTML")
	public ResponseEntity<StreamingResponseBody> getTestHTML(final HttpServletResponse response) {
		log.info("testHTML ");

		final String rptDesign = "param_test.rptdesign";
		final String outputName = "Test_Parameter_Report.pdf";
		final String format = "HTML";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramString", "Ginger");
		params.put("paramDate", "2010-09-09");
		params.put("paramBoolean", true);
		params.put("paramDecimal", 1111.3333);
		params.put("paramInteger", 98765);
		final ExecuteRequest request = new ExecuteRequest(rptDesign, outputName, format, params, true);
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
	@GetMapping(value = "/runReport", produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<StreamingResponseBody> executeRunReport(@RequestBody final ExecuteRequest request,
			final HttpServletResponse response) {

		log.info("Run Report: " + Thread.currentThread());
		final StreamingResponseBody responseBody = out -> {

			log.info("Run Report Lambda: " + Thread.currentThread());
			try {
				runner.execute(request, response);
			} catch (final BirtStarterException e) {
				e.sendError(response, request.getWrapError());
			}
		};

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + request.getOutputName() + "." + request.getFormat())
				.contentType(Util.getMediaType(request.getFormat())).body(responseBody);
	}

}
