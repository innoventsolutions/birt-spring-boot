/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package sample.birt.entity;

import org.quartz.JobKey;

public class ScheduleResponse {
	private final JobKey jobKey;
	private final String message;

	public ScheduleResponse(final JobKey jobKey, final String message) {
		this.jobKey = jobKey;
		this.message = message;
	}

	public JobKey getJobKey() {
		return jobKey;
	}

	public String getMessage() {
		return message;
	}
}
