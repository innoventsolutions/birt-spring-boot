package com.innoventsolutions.birt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class JobController {

	@Autowired
	public JobController() {
		log.info("Create Job Controller");
	}

	@Autowired
	private ReportRunService runner;
	@Autowired
	private SubmitJobService submitter;

	@GetMapping("/testPDF")
	public ResponseEntity<StreamingResponseBody> getTestPDF(final HttpServletResponse response) {
		log.info("testPDF ");

		final String rptDesign = "TEST";
		final String humanName = "Test Report";
		final String format = "PDF";
		final ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format);

		return executeRunReport(request, response);

	}

	@GetMapping("/testHTML")
	public ResponseEntity<StreamingResponseBody> getTestHTML(final HttpServletResponse response) {
		log.info("testHTML ");

		final String rptDesign = "param_test.rptdesign";
		final String humanName = "Test Parameter Report";
		final String format = "HTML";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramString", "Ginger");
		params.put("paramDate", "2010-09-09");
		params.put("paramBoolean", true);
		params.put("paramDecimal", 1111.3333);
		params.put("paramInteger", 98765);
		final ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format, params);

		return executeRunReport(request, response);

	}

	@GetMapping("/runReport")
	private ResponseEntity<StreamingResponseBody> executeRunReport(@RequestBody final ExecuteRequest request,
			final HttpServletResponse response) {
		final StreamingResponseBody stream = out -> {
			runner.execute(request, response);
		};
		log.info("steaming response {} ", stream);
		return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
	}
	
	@GetMapping("/testSubmit")
	public ResponseEntity<SubmitResponse> getTestSubmit(final HttpServletResponse htppResponse) {
		log.info("testSubmit ");
		
		SubmitResponse outerResponse = new SubmitResponse(new ExecuteRequest());

		final String rptDesign = "param_test.rptdesign";
		int min = 1;
		int max = 10;
		for (int i = 0; i < 100; i++) {
			
			int delay = ((int) (Math.random()*(max - min))) + min;
			final String outputName = "Test_" + i;
			String format = "PDF";
			if ((i % 2)/2 == 0)
				format = "HTML";
			
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("paramString", "Ginger");
			params.put("paramDate", "2010-09-09");
			params.put("paramBoolean", true);
			params.put("paramDecimal", (i*1.9)*i);
			params.put("paramInteger", i);
			params.put("delay", delay);
			final ExecuteRequest request = new ExecuteRequest(rptDesign, outputName, format, params);

			executeSubmitJob(request, htppResponse);
			
		}

		return new ResponseEntity<SubmitResponse>(outerResponse, HttpStatus.OK);

	}

	@GetMapping("/submitReport")
	private ResponseEntity<SubmitResponse> executeSubmitJob(@RequestBody final ExecuteRequest request,
			final HttpServletResponse httpResponse) {

		SubmitResponse submitResponse = new SubmitResponse(request);
		CompletableFuture.supplyAsync(() ->  submitter.executeRunThenRender(submitResponse, httpResponse));
		
		
		return new ResponseEntity<SubmitResponse>(submitResponse, HttpStatus.OK);
	}

}
