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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import sample.birt.entity.GetJobRequest;
import sample.birt.entity.GetJobResponse;
import sample.birt.entity.ScheduleCronRequest;
import sample.birt.entity.ScheduleResponse;
import sample.birt.quartz.RunReportQuartzJob;
import sample.birt.service.CompletedJobList;

@Slf4j
@Controller
public class SampleJobController {

	@PostMapping("/schedule-cron")
	@ResponseBody
	public ResponseEntity<ScheduleResponse> scheduleCron(@RequestBody final ScheduleCronRequest request) {
		log.info("schedule-cron " + request);
		try {
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (scheduler.isShutdown()) {
				throw new RuntimeException("Scheduler has been shut down");
			}
			final JobDetail jobDetail = JobBuilder.newJob(RunReportQuartzJob.class)
					.withIdentity(request.getName(), request.getGroup()).build();
			final JobDataMap jobDataMap = jobDetail.getJobDataMap();
			jobDataMap.put("submitRequest", request);
			final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
					.withIdentity(request.getName() + "-trigger", request.getGroup());
			final Date startDate = request.getStartDate();
			log.info("startDate = " + startDate);
			if (startDate == null) {
				triggerBuilder.startNow();
			} else {
				triggerBuilder.startAt(startDate);
			}
			final String cronString = request.getCronString();
			log.info("cronString = " + cronString);
			final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronString);
			final String misfireInstruction = request.getMisfireInstruction();
			log.info("misfireInstruction = " + misfireInstruction);
			if ("ignore".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
			} else if ("fire-and-proceed".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
			} else if ("do-nothing".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionDoNothing();
			} else if (misfireInstruction != null) {
				throw new RuntimeException("Unrecognized misfire instruction");
			}
			triggerBuilder.withSchedule(scheduleBuilder);
			final Trigger trigger = triggerBuilder.build();
			scheduler.scheduleJob(jobDetail, trigger);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(trigger.getJobKey(), null), HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (final Throwable e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/job")
	@ResponseBody
	public ResponseEntity<DeleteJobResponse> deleteJob(@RequestBody final GetJobRequest request) {
		log.info("delete-job " + request);
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			final JobKey jobKey = new JobKey(request.getName(), request.getGroup());
			final boolean result = scheduler.deleteJob(jobKey);
			return new ResponseEntity<DeleteJobResponse>(new DeleteJobResponse(result), HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<DeleteJobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Autowired
	private CompletedJobList completedJobList;

	@GetMapping("/completed-job")
	@ResponseBody
	public ResponseEntity<GetJobResponse> getJob(@RequestBody final GetJobRequest request) {
		log.info("job " + request);
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			final JobKey jobKey = new JobKey(request.getName(), request.getGroup());
			final GetJobResponse jobResponse = new GetJobResponse();
			// @SuppressWarnings("unchecked")
			// final List<Trigger> triggers = (List<Trigger>)
			// scheduler.getTriggersOfJob(jobKey);
			// jobResponse.setTriggers(triggers);
			jobResponse.setRuns(completedJobList.getJob(jobKey));

			return new ResponseEntity<GetJobResponse>(jobResponse, HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<GetJobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/completed-jobs")
	@ResponseBody
	public ResponseEntity<Map<JobKey, GetJobResponse>> getJobs() {
		log.info("jobs");
		final Map<JobKey, GetJobResponse> response = new HashMap<>();
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
				final GetJobResponse jobResponse = new GetJobResponse();
				// @SuppressWarnings("unchecked")
				// final List<Trigger> triggers = (List<Trigger>)
				// scheduler.getTriggersOfJob(jobKey);
				// jobResponse.setTriggers(triggers);
				jobResponse.setRuns(completedJobList.getJob(jobKey));
				response.put(jobKey, jobResponse);
			}
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<Map<JobKey, GetJobResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<JobKey, GetJobResponse>>(response, HttpStatus.OK);
	}

}
