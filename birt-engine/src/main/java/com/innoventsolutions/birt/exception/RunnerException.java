package com.innoventsolutions.birt.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class RunnerException extends Exception {
	private static final long serialVersionUID = 1L;

	public RunnerException() {
		super();
	}

	public RunnerException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		log.error(message);
	}

	public RunnerException(final String message, final Throwable cause) {
		super(message, cause);
		log.error(message);
	}

	public RunnerException(final String message) {
		super(message);
		log.error(message);
	}

	public RunnerException(final Throwable cause) {
		super(cause);
		log.error(cause.getMessage());
	}

}
