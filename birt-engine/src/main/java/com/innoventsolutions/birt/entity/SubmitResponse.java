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
	private String jobid;
	private String rptDocName;
	private String outFileName;
	private Date submitTime;
	private Date runBegin;
	private Date runFinish;
	private Date renderBegin;
	private Date renderFinish;
	private StatusEnum status;
	private ExecuteRequest request;
	private HttpStatus httpStatus;
	private String httpStatusMessage;

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
