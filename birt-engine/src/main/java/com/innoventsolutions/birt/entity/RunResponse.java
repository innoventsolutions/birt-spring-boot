package com.innoventsolutions.birt.entity;

import java.util.Date;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
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
		final long id = submitTime.getTime();
		this.outFileName = request.getOutputName() + "." + request.getFormat();
		this.status = StatusEnum.INIT;
		this.httpStatus = HttpStatus.OK;

		log.info("Create response for: " + outFileName);
	}

	public enum StatusEnum {
		INIT, RUNANDRENDER, COMPLETE, CANCELLED, EXCEPTION, UNKNOWN
	}

}
