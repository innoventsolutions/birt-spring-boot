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
import org.springframework.stereotype.Component;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;
import sample.birt.entity.EmailRequest;
import sample.birt.entity.ExtendedExecuteRequest;
import sample.birt.entity.ExtendedSubmitResponse;
import sample.birt.service.EmailService;
import sample.birt.service.StartedJobList;

@Slf4j
@Component
public class RunReportQuartzJob implements Job {
	@Autowired
	private SubmitJobService submitJobService;

	@Autowired
	private StartedJobList startedJobList;

	@Autowired
	private EmailService emailService;

	@Autowired
	private BirtConfig birtConfig;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDetail jobDetail = context.getJobDetail();
		final JobKey jobKey = jobDetail.getKey(); // identifies the job (name+group)
		final JobDataMap jobDataMap = context.getMergedJobDataMap();
		final ExtendedExecuteRequest request = (ExtendedExecuteRequest) jobDataMap.get("submitRequest");
		if (request == null) {
			throw new RuntimeException("submitRequest not found in jobDataMap");
		}
		if (submitJobService == null) {
			log.info("submitJobService not autowired");
			submitJobService = (SubmitJobService) jobDataMap.get("submitJobService");
		}
		if (submitJobService == null) {
			throw new RuntimeException("submitJobService not found");
		}
		if (startedJobList == null) {
			log.info("startedJobList not autwired");
			startedJobList = (StartedJobList) jobDataMap.get("startedJobList");
		}
		if (startedJobList == null) {
			throw new RuntimeException("startedJobList not found");
		}
		if (birtConfig == null) {
			log.info("birtConfig not autowired");
			birtConfig = (BirtConfig) jobDataMap.get("birtConfig");
		}
		if (birtConfig == null) {
			throw new RuntimeException("birtConfig not found");
		}
		final EmailRequest emailRequest = request.getEmail();
		if (emailRequest != null) {
			if (emailService == null) {
				log.info("emailService not autowired");
				emailService = (EmailService) jobDataMap.get("emailService");
			}
			if (emailService == null) {
				throw new RuntimeException("emailService not found");
			}
		}
		try {
			final ExtendedSubmitResponse response = new ExtendedSubmitResponse(request);
			final CompletableFuture<SubmitResponse> future = submitJobService.executeRunThenRender(response);
			if (emailRequest != null) {
				future.thenApply(l -> emailService.send(emailRequest, response));
			}
			startedJobList.addJob(jobKey, future);
		} catch (final Throwable e) {
			throw new JobExecutionException("Failed to submit report", e, false);
		}
	}
}
