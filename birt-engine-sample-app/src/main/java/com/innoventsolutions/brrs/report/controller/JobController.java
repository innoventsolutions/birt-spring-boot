/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brrs.report.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.innoventsolutions.brrs.report.ReportRun;
import com.innoventsolutions.brrs.report.ReportRunStatus;
import com.innoventsolutions.brrs.report.entity.BaseRequest;
import com.innoventsolutions.brrs.report.entity.RunRequest;
import com.innoventsolutions.brrs.report.exception.BadRequestException;
import com.innoventsolutions.brrs.report.service.RunnerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class JobController {
	@Autowired
	private RunnerService runner;

	@GetMapping("/test")
	@ResponseBody
	public ResponseEntity<Map<UUID, ReportRunStatus>> getTest(@RequestBody final BaseRequest request) {
		log.info("getTest " + request);
		final Map<UUID, ReportRunStatus> status = runner.getStatusAll();
		return new ResponseEntity<Map<UUID, ReportRunStatus>>(status, HttpStatus.OK);
	}

	@PostMapping("/run")
	@ResponseBody
	public ResponseEntity<Resource> run(@RequestBody final RunRequest request) {
		log.info("run");
		try {
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = UUID.randomUUID() + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(), null, format, outputFilename,
					request.isRunThenRender(), runner.fixParameterTypes(request.getParameters()));
			final List<Exception> exceptions = runner.runReport(reportRun);
			if (!exceptions.isEmpty()) {
				for (final Throwable e : exceptions) {
					log.error("Exception", e);
				}
				return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final File outputDir = runner.getOutputDirectory();
			final File outputFile = outputDir == null ? new File(outputFilename) : new File(outputDir, outputFilename);
			final InputStream inputStream = new FileInputStream(outputFile);
			final HttpHeaders headers = new HttpHeaders();
			// headers.set("Content-Disposition", "attachment; filename=\"" + outputFilename
			// + "\"");
			final InputStreamResource resource = new InputStreamResource(inputStream);
			return ResponseEntity.ok().headers(headers).contentLength(outputFile.length())
					.contentType(getMediaType(format)).body(resource);
		} catch (final BadRequestException e) {
			return getErrorResponse(HttpStatus.valueOf(e.getCode()), e.getReason());
		} catch (final Throwable e) {
			log.error("Exception", e);
			return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	private MediaType getMediaType(final String format) {
		if ("pdf".equalsIgnoreCase(format)) {
			return MediaType.APPLICATION_PDF;
		}
		if ("html".equalsIgnoreCase(format)) {
			return MediaType.TEXT_HTML;
		}
		if ("xls".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-excel");
		}
		if ("xlsx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
		if ("doc".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/ms-word");
		}
		if ("docx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		if ("ppt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-powerpoint");
		}
		if ("pptx".equalsIgnoreCase(format)) {
			return MediaType
					.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
		}
		if (".odp".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.presentation");
		}
		if (".ods".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet");
		}
		if (".odt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.text");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	public static ResponseEntity<Resource> getErrorResponse(final HttpStatus code, final String reason) {
		final HttpHeaders headers = new HttpHeaders();
		final ByteArrayResource resource = new ByteArrayResource(reason.getBytes());
		return ResponseEntity.status(code).headers(headers).contentType(MediaType.TEXT_PLAIN).body(resource);
	}

}
