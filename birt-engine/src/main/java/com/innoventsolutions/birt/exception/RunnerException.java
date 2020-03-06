package com.innoventsolutions.birt.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunnerException extends Exception {
	private static final long serialVersionUID = 1L;

	public RunnerException() {
		super();
	}

	public RunnerException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RunnerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RunnerException(final String message) {
		super(message);
	}

	public RunnerException(final Throwable cause) {
		super(cause);
	}

}
