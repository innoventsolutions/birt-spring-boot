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
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;
import sample.birt.entity.ExecuteAndEmailRequest;
import sample.birt.entity.SubmitAndEmailResponse;
import sample.birt.service.EmailService;

@Slf4j
@Controller
public class SampleJobController {

	@Autowired
	private SubmitJobService submitJobService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ExecutorService executorService;

	private final Map<String, CompletableFuture<SubmitResponse>> submitList = new HashMap<String, CompletableFuture<SubmitResponse>>();

	@GetMapping("/submitJobEmail")
	public ResponseEntity<SubmitAndEmailResponse> executeSubmitJob(@RequestBody final ExecuteAndEmailRequest request,
			final HttpServletResponse httpResponse) {

		final SubmitAndEmailResponse submitAndEmailResponse = new SubmitAndEmailResponse(request);
		final CompletableFuture<SubmitResponse> future = submitJobService.executeRunThenRender(submitAndEmailResponse);
		future.thenApply(l -> emailService.send(request.getEmail(), submitAndEmailResponse));
		submitList.put(submitAndEmailResponse.getJobid(), future);

		return new ResponseEntity<SubmitAndEmailResponse>(submitAndEmailResponse, HttpStatus.OK);
	}

}
