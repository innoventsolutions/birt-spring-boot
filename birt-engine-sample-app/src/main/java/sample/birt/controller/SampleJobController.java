/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package sample.birt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SampleJobController {
	@Autowired
	private SubmitJobService submitter;

	@GetMapping("/test")
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
			final ExecuteRequest request = new ExecuteRequest(rptDesign, outputName, format, params);

			executeSubmitJob(request, httpResponse);

		}

		return new ResponseEntity<SubmitResponse>(outerResponse, HttpStatus.OK);

	}

	@GetMapping("/submitJob")
	public ResponseEntity<SubmitResponse> executeSubmitJob(@RequestBody final ExecuteRequest request,
			final HttpServletResponse httpResponse) {

		final SubmitResponse submitResponse = new SubmitResponse(request);
		@SuppressWarnings("unused")
		final CompletableFuture<SubmitResponse> submission = submitter.executeRunThenRender(submitResponse);

		return new ResponseEntity<SubmitResponse>(submitResponse, HttpStatus.OK);
	}

}
