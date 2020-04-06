/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.entity;

import java.util.Date;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Used as response for RunReport operations which are synchronous
 * RunReport operations will be performed in unique threads, but the response should contain the report content.
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Getter @Setter @Slf4j
public class RunResponse {
	private String outFileName;
	private Date submitTime;
	private Date finish;
	private StatusEnum status;
	private HttpStatus httpStatus;
	private String httpStatusMessage;
	private ExecuteRequest request;

	public RunResponse(final ExecuteRequest request) {
		this.request = request;
		this.submitTime = new Date();
		this.outFileName = request.getOutputName() + "." + request.getFormat();
		this.status = StatusEnum.INIT;
		this.httpStatus = HttpStatus.OK;

		log.info("Create response for: " + outFileName);
	}

	public enum StatusEnum {
		INIT, RUNANDRENDER, COMPLETE, CANCELLED, EXCEPTION, UNKNOWN
	}

}
