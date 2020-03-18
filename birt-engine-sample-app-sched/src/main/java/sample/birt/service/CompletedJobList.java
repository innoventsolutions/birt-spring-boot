/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package sample.birt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.quartz.JobKey;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.entity.SubmitResponse;

@Service
public class CompletedJobList {
	private final Map<JobKey, List<CompletableFuture<SubmitResponse>>> jobs = new HashMap<>();

	public void addJob(final JobKey jobKey, final CompletableFuture<SubmitResponse> future) {
		List<CompletableFuture<SubmitResponse>> futures = jobs.get(jobKey);
		if (futures == null) {
			futures = new ArrayList<>();
			jobs.put(jobKey, futures);
		}
		futures.add(future);
	}

	public List<CompletableFuture<SubmitResponse>> getJob(final JobKey jobKey) {
		return jobs.get(jobKey);
	}
}
