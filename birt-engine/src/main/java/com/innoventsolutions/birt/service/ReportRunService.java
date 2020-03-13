/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.UnsupportedFormatException;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BadRequestException;
import com.innoventsolutions.birt.exception.RunnerException;
import com.innoventsolutions.birt.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Import(BirtEngineService.class)
public class ReportRunService extends BaseReportService {

	public ReportRunService() {
		log.info("Start RunService");
	}

	@SuppressWarnings("unchecked")
	public void execute(final ExecuteRequest request, final HttpServletResponse response) {
		log.info("runReport reportRun = " + request);
		try {
			final OutputStream oStream = response.getOutputStream();
			final IReportRunnable design = getRunnableReportDesign(request);
			// Run Reports will only do a RunAndRender
			final IRunAndRenderTask rrTask = engineService.getEngine().createRunAndRenderTask(design);
			// TODO Does not make sense
			final Map<String, Object> appContext = rrTask.getAppContext();
			rrTask.setAppContext(appContext);

			configureParameters(request, design, rrTask);

			log.info("getRenderOptions");
			final String format = request.format;
			final RenderOption options = configureRenderOptions(format);
			response.setContentType(Util.getMediaType(format).toString());
			response.setHeader("Content-Disposition", "attachment;filename=" + request.getOutputName() + "." + format);

			options.setOutputStream(oStream);

			rrTask.setRenderOption(options);
			log.info("run-and-render report");
			try {
				rrTask.run();
			} catch (final UnsupportedFormatException e) {
				throw new BadRequestException(406, "Unsupported output format");
			} catch (final Exception e) {
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException"
						.equals(e.getClass().getName())) {
					throw new BadRequestException(406, e.getMessage());
				}
				throw new RunnerException("Run-and-render task failed", e);
			}
			final List<Exception> exceptions = new ArrayList<>();
			final List<EngineException> errors = rrTask.getErrors();
			if (errors != null && errors.size() > 0) {
				for (final EngineException exception : errors) {
					exceptions.add(exception);
				}

			}
		} catch (final BadRequestException e1) {
			try {
				response.sendError(e1.getCode(), e1.getReason());
			} catch (final IOException e) {
				log.error("Unable to send error code " + e1.getCode(), e);
			}
		} catch (final Exception e1) {
			try {
				log.error("Unable to run report", e1);
				response.sendError(500, e1.getMessage());
			} catch (final IOException e) {
				log.error("Unable to send error code 500", e);
			}
		}

	}

}
