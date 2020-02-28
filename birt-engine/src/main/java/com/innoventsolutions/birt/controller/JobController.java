package com.innoventsolutions.birt.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping("/test")
	public ResponseEntity<StreamingResponseBody> getTest(final HttpServletResponse response) {
		log.info("getTest ");

		String rptDesign = "TEST";
		String humanName = "Test Report";
		String format = "PDF";
		ExecuteRequest request = new ExecuteRequest(rptDesign, humanName, format);

		response.setContentType(MediaType.APPLICATION_PDF.toString());
		response.setHeader("Content-Disposition", "attachment;filename=" + request.getNameForHumans());
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
