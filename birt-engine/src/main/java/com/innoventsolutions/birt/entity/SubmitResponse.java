package com.innoventsolutions.birt.entity;

import java.util.Date;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class SubmitResponse {
	protected String jobid;
	protected String rptDocName;
	protected String outFileName;
	protected Date submitTime;
	protected Date runBegin;
	protected Date runFinish;
	protected Date renderBegin;
	protected Date renderFinish;
	protected StatusEnum status;
	protected ExecuteRequest request;
	protected HttpStatus httpStatus;
	protected String httpStatusMessage;

	public SubmitResponse(final ExecuteRequest request) {
		this.request = request;
		this.submitTime = new Date();
		final long id = submitTime.getTime();
		this.jobid = request.getOutputName() + "_" + String.valueOf(id);
		this.rptDocName = jobid + ".rptdocument";
		this.outFileName = jobid + "." + request.getFormat();
		this.status = StatusEnum.INIT;
		this.httpStatus = HttpStatus.OK;

		log.info("Create response for: " + rptDocName);
	}

	public enum StatusEnum {
		INIT, RUN, RENDER, COMPLETE, CANCELLED, EXCEPTION, UNKNOWN
	}

}
