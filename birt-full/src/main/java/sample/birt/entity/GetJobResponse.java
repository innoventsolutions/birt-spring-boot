package sample.birt.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import com.innoventsolutions.birt.entity.SubmitResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetJobResponse {
	// List<Trigger> triggers;
	JobKey jobKey;
	String description;
	Class<? extends Job> jobClass;
	JobDataMap jobDataMap;
	List<CompletableFuture<SubmitResponse>> runs;
	List<TriggerResponse> triggers;

	@Getter
	@Setter
	public static class TriggerResponse {
		String calendarName;
		String description;
		Date endTime;
		Date finalFireTime;
		Date nextFireTime;
		Date previousFireTime;
		Date startTime;
		TriggerKey triggerKey;
		int misfireInstruction;
		int priority;
	}

	public void setJobDetail(final JobDetail jobDetail) {
		if (jobDetail != null) {
			jobKey = jobDetail.getKey();
			description = jobDetail.getDescription();
			jobClass = jobDetail.getJobClass();
			jobDataMap = jobDetail.getJobDataMap();
		}
	}

	public void setTriggers(final List<Trigger> triggers) {
		this.triggers = new ArrayList<>();
		for (final Trigger trigger : triggers) {
			final TriggerResponse triggerResponse = new TriggerResponse();
			triggerResponse.setCalendarName(trigger.getCalendarName());
			triggerResponse.setDescription(trigger.getDescription());
			triggerResponse.setEndTime(trigger.getEndTime());
			triggerResponse.setFinalFireTime(trigger.getFinalFireTime());
			triggerResponse.setNextFireTime(trigger.getNextFireTime());
			triggerResponse.setTriggerKey(trigger.getKey());
			triggerResponse.setMisfireInstruction(trigger.getMisfireInstruction());
			triggerResponse.setPreviousFireTime(trigger.getPreviousFireTime());
			triggerResponse.setPriority(trigger.getPriority());
			triggerResponse.setStartTime(trigger.getStartTime());
			this.triggers.add(triggerResponse);
		}
	}
}