/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package sample.birt.quartz;

import java.util.concurrent.CompletableFuture;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

import sample.birt.service.CompletedJobList;

public class RunReportQuartzJob implements Job {
	@Autowired
	private SubmitJobService submitJobService;

	@Autowired
	private CompletedJobList completedJobList;

	@Autowired
	private BirtConfig birtConfig;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDetail jobDetail = context.getJobDetail();
		final JobKey jobKey = jobDetail.getKey(); // identifies the job (name+group)
		final JobDataMap jobDataMap = context.getMergedJobDataMap();
		final ExecuteRequest request = (ExecuteRequest) jobDataMap.get("submitRequest");
		if (request == null) {
			throw new RuntimeException("submitRequest not found in jobDataMap");
		}
		if (submitJobService == null) {
			throw new RuntimeException("submitJobService not autowired");
		}
		if (completedJobList == null) {
			throw new RuntimeException("schedulerService not autowired");
		}
		if (birtConfig == null) {
			throw new RuntimeException("birtConfig not autowired");
		}
		try {
			final SubmitResponse submitResponse = new SubmitResponse(request);
			final CompletableFuture<SubmitResponse> future = submitJobService.executeRunThenRender(submitResponse);
			completedJobList.addJob(jobKey, future);
		} catch (final Throwable e) {
			throw new JobExecutionException("Failed to submit report", e, false);
		}
	}
}
