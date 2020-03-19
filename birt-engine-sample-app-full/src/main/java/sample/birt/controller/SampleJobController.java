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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;
import sample.birt.entity.DeleteJobResponse;
import sample.birt.entity.EmailRequest;
import sample.birt.entity.ExtendedExecuteRequest;
import sample.birt.entity.ExtendedSubmitResponse;
import sample.birt.entity.GetJobRequest;
import sample.birt.entity.GetJobResponse;
import sample.birt.entity.ScheduleRequest;
import sample.birt.quartz.RunReportQuartzJob;
import sample.birt.service.EmailService;
import sample.birt.service.StartedJobList;

@Slf4j
@Controller
public class SampleJobController {

	@Autowired
	private SubmitJobService submitJobService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private StartedJobList startedJobList;

	@Autowired
	private BirtConfig birtConfig;

	// does not work @Autowired
	// private Scheduler scheduler;

	private final Map<String, CompletableFuture<SubmitResponse>> submitList = new HashMap<String, CompletableFuture<SubmitResponse>>();

	@GetMapping("/schedule")
	public ResponseEntity<ExtendedSubmitResponse> executeSubmitJob(@RequestBody final ExtendedExecuteRequest request) {
		log.info("/schedule " + request);

		final ScheduleRequest scheduleRequest = request.getSchedule();
		final ExtendedSubmitResponse response = new ExtendedSubmitResponse(request);
		if (scheduleRequest != null) {
			try {
				final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
				if (!scheduler.isStarted()) {
					scheduler.start();
				}
				if (scheduler.isShutdown()) {
					throw new RuntimeException("Scheduler has been shut down");
				}
				final JobDetail jobDetail = JobBuilder.newJob(RunReportQuartzJob.class)
						.withIdentity(scheduleRequest.getName(), scheduleRequest.getGroup()).build();
				final JobDataMap jobDataMap = jobDetail.getJobDataMap();
				jobDataMap.put("submitRequest", request);
				jobDataMap.put("submitJobService", submitJobService);
				jobDataMap.put("emailService", emailService);
				jobDataMap.put("startedJobList", startedJobList);
				jobDataMap.put("birtConfig", birtConfig);
				final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
						.withIdentity(scheduleRequest.getName() + "-trigger", scheduleRequest.getGroup());
				final Date startDate = scheduleRequest.getStartDate();
				log.info("startDate = " + startDate);
				if (startDate == null) {
					triggerBuilder.startNow();
				} else {
					triggerBuilder.startAt(startDate);
				}
				String cronString = scheduleRequest.getCronString();
				if (cronString == null) {
					cronString = getCronString(System.currentTimeMillis() + 5000);
				} else if (cronString.indexOf(" ") < 0 && cronString.startsWith("+")) {
					cronString = getCronString(System.currentTimeMillis() + Long.parseLong(cronString.substring(1)));
				}
				log.info("cronString = " + cronString);
				final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronString);
				final String misfireInstruction = scheduleRequest.getMisfireInstruction();
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
				response.setJobKey(trigger.getJobKey());
				return new ResponseEntity<ExtendedSubmitResponse>(response, HttpStatus.OK);
			} catch (final SchedulerException e) {
				log.error("Exception", e);
				response.setHttpStatus(HttpStatus.BAD_REQUEST);
				response.setHttpStatusMessage(e.getMessage());
				return new ResponseEntity<ExtendedSubmitResponse>(response, HttpStatus.BAD_REQUEST);
			} catch (final Throwable e) {
				log.error("Exception", e);
				response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
				response.setHttpStatusMessage(e.getMessage());
				return new ResponseEntity<ExtendedSubmitResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		log.info("run immediately");
		final CompletableFuture<SubmitResponse> future = submitJobService.executeRunThenRender(response);
		final EmailRequest emailRequest = request.getEmail();
		if (emailRequest != null) {
			future.thenApply(l -> emailService.send(emailRequest, response));
		}
		submitList.put(response.getJobid(), future);
		return new ResponseEntity<ExtendedSubmitResponse>(response, HttpStatus.OK);
	}

	public static String getCronString(final long time) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		final List<String> args = new ArrayList<>();
		args.add(String.valueOf(cal.get(Calendar.SECOND)));
		args.add(String.valueOf(cal.get(Calendar.MINUTE)));
		args.add(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
		args.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
		args.add(String.valueOf(cal.get(Calendar.MONTH) + 1)); // note: javadocs are wrong for this
		args.add("?"); // day of week
		args.add(String.valueOf(cal.get(Calendar.YEAR)));
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final String arg : args) {
			sb.append(sep);
			sep = " ";
			sb.append(arg);
		}
		return sb.toString();
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

	@GetMapping("/job")
	@ResponseBody
	public ResponseEntity<GetJobResponse> getJob(@RequestBody final GetJobRequest request) {
		log.info("job " + request);
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			final JobKey jobKey = new JobKey(request.getName(), request.getGroup());
			final GetJobResponse jobResponse = new GetJobResponse();
			@SuppressWarnings("unchecked")
			final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
			jobResponse.setTriggers(triggers);
			jobResponse.setJobDetail(scheduler.getJobDetail(jobKey));
			jobResponse.setRuns(startedJobList.getJob(jobKey));

			return new ResponseEntity<GetJobResponse>(jobResponse, HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<GetJobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/jobs")
	@ResponseBody
	public ResponseEntity<Map<JobKey, GetJobResponse>> getJobs() {
		log.info("jobs");
		final Map<JobKey, GetJobResponse> response = new HashMap<>();
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
				final GetJobResponse jobResponse = new GetJobResponse();
				@SuppressWarnings("unchecked")
				final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
				jobResponse.setTriggers(triggers);
				jobResponse.setJobDetail(scheduler.getJobDetail(jobKey));
				jobResponse.setRuns(startedJobList.getJob(jobKey));
				response.put(jobKey, jobResponse);
			}
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<Map<JobKey, GetJobResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<JobKey, GetJobResponse>>(response, HttpStatus.OK);
	}
}
