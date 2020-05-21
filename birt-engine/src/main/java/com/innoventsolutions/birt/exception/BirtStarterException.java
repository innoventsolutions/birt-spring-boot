/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to wrap all Exceptions thrown by BIRT-ENGINE
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Slf4j
public class BirtStarterException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private BirtErrorCode errorCode;
	private final List<Exception> exceptions = new ArrayList<>();

	public BirtStarterException(final BirtErrorCode errorCode, final String message) {
		super(message);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}

	public BirtStarterException(final BirtErrorCode errorCode, final String message, final Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}

	public BirtStarterException(final BirtErrorCode errorCode, final Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
		log.error(getErrorMessage(), this);
	}

	public BirtStarterException(final BirtErrorCode errorCode, final List<EngineException> errors) {
		super("Engine Exception");
		for (final EngineException exception : errors) {
			exceptions.add(exception);
		}
	}

	private String getErrorMessage() {
		final StringBuffer sb = new StringBuffer();
		sb.append("BirtStarterException: ").append(errorCode).append(":").append(this.getMessage());
		if (exceptions.size() > 0) {
			for (final Exception exception : exceptions) {
				sb.append(exception.getMessage());
			}
		}
		return sb.toString();

	}

	public String getErrorCode() {
		return this.errorCode.toString();
	}

	public HttpStatus getHttpCode() {
		return errorCode.getHttpStatus();
	}

	@Getter
	public enum BirtErrorCode {
		PLATFORM_START(HttpStatus.INTERNAL_SERVER_ERROR), BAD_REQUEST(HttpStatus.BAD_REQUEST),
		UNKNOWN_PARAMETER(HttpStatus.BAD_REQUEST), PARAMETER_VALIDATION(HttpStatus.BAD_REQUEST),
		BAD_FORMAT(HttpStatus.UNSUPPORTED_MEDIA_TYPE), DESIGN_FILE_LOCATION(HttpStatus.NOT_FOUND),
		RUNANDRENDER_TASK(HttpStatus.INTERNAL_SERVER_ERROR), RUN_TASK(HttpStatus.INTERNAL_SERVER_ERROR),
		RENDER_TASK(HttpStatus.INTERNAL_SERVER_ERROR);

		private final HttpStatus httpStatus;

		private BirtErrorCode(final HttpStatus httpStatus) {
			this.httpStatus = httpStatus;
		}
	}
}
