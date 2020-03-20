/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.exception;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.engine.api.EngineException;
import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BirtStarterException extends Exception {
	private static final long serialVersionUID = 1L;
	private BirtErrorCode errorCode; 
	private final List<Exception> exceptions = new ArrayList<>();
	
	public BirtStarterException(BirtErrorCode errorCode, final String message) {
		super(message);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}

	public BirtStarterException(BirtErrorCode errorCode, final String message, final Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}

	public BirtStarterException(BirtErrorCode errorCode,  final Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}
	
	public BirtStarterException(BirtErrorCode errorCode, List<EngineException> errors) {
		super("Engine Exception");
		for (final EngineException exception : errors) {
			exceptions.add(exception);
		}
	}
	
	private String getErrorMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("BirtStarterException: ").append(errorCode).append(":").append(this.getMessage());
		if (exceptions.size() > 0 ) {
			for (Exception exception : exceptions) {
				sb.append(exception.getMessage());
			}
		}
		return sb.toString();
		
	}
	
	public String getErrorCode() {
		return this.errorCode.toString();
	}
	
	public HttpStatus getHttpCode() {
		
		return HttpStatus.OK;
	}
	
	public enum BirtErrorCode { PLATFORM_START, BAD_REQUEST , UNKNOWN_PARAMETER , PARAMETER_VALIDATION, BAD_FORMAT, DESIGN_FILE_LOCATION , RUNANDRENDER_TASK, RUN_TASK, RENDER_TASK}; 

}
