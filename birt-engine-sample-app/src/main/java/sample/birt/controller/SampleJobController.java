/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

@Controller
public class SampleJobController {
	
	@Autowired
	private SubmitJobService submitter;

	@GetMapping("/")
	public ResponseEntity<String> getRoot() {

		return getWelcome();

	}

	@GetMapping("/welcome")
	public ResponseEntity<String> getWelcome() {
		System.out.println("Welcome ");
		
		String welcome_msg = "<h1>Welcome to the BIRT Starter sample app.</h1><p>Try /test, or /testSubmit to exercise any of the built in BIRT REST endpoints.</p><p><a href=\"/index.html\">/index.html</a> provides documentation of the rest endpoints</p>";
		
		return new ResponseEntity<String>(welcome_msg, HttpStatus.OK);

	}
	
	@GetMapping("/test")
	public ResponseEntity<SubmitResponse> getTestSubmit(@RequestParam(required = false) Integer numToRun,
			final HttpServletResponse httpResponse) {
		
		/* Example of using birt-engine service classes directly 
		 *
		 * Built in REST end points exist at /runReport and /submitJob
		 */

		System.out.println("testSubmit Outer:");

		
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
			final ExecuteRequest request = new ExecuteRequest(rptDesign, outputName, format, params, true);

			executeSubmitJob(request, httpResponse);

		}

		return new ResponseEntity<SubmitResponse>(outerResponse, HttpStatus.OK);

	}

	@PostMapping("/sampleSubmitJob")
	public ResponseEntity<SubmitResponse> executeSubmitJob(@RequestBody final ExecuteRequest request,
			final HttpServletResponse httpResponse) {

		System.out.println("Submit Job Inner:");

		final SubmitResponse submitResponse = new SubmitResponse(request);
		@SuppressWarnings("unused")
		final CompletableFuture<SubmitResponse> submission = submitter.executeRunThenRender(submitResponse);

		return new ResponseEntity<SubmitResponse>(submitResponse, HttpStatus.OK);
	}

}
