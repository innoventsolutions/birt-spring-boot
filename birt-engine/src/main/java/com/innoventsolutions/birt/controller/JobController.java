package com.innoventsolutions.birt.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BadRequestException;
import com.innoventsolutions.birt.service.ReportRunService;

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

	@GetMapping("/testPDF")
	public ResponseEntity<StreamingResponseBody> getTestPDF(final HttpServletResponse response) {
		log.info("getTest ");

		String rptDesign = "TEST";
		String humanName = "Test Report";
		String format = "PDF";
		ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format);

		return executeRunReport(request, response);

	}
	
	@GetMapping("/testHTML")
	public ResponseEntity<StreamingResponseBody> getTestHTML(final HttpServletResponse response) {
		log.info("getTest ");

		String rptDesign = "param_test.rptdesign";
		String humanName = "Test Parameter Report";
		String format = "HTML";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramString", "Ginger");
		params.put("paramDate", "2010-09-09");
		params.put("paramBoolean", true);
		params.put("paramDecimal", 1111.3333);
		params.put("paramInteger", 98765); 
		ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format, params);

		return executeRunReport(request, response);

	}
	
	@GetMapping("/runReport")
	private ResponseEntity<StreamingResponseBody> executeRunReport(@RequestBody final ExecuteRequest request, final HttpServletResponse response) {
		StreamingResponseBody stream = out -> {
			try {
				
				runner.execute( request, response);
				
			} catch (BadRequestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
		log.info("steaming response {} ", stream);
		return new ResponseEntity(stream, HttpStatus.OK);
	}


}
