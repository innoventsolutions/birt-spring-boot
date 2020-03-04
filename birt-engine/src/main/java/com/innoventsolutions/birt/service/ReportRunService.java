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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.UnsupportedFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BadRequestException;
import com.innoventsolutions.birt.exception.RunnerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportRunService extends BaseReportService {
	@Autowired
	public ReportRunService() {
		log.info("Start RunService");
	}

	@SuppressWarnings("unchecked")
	public void execute(final ExecuteRequest request, final HttpServletResponse response) throws BadRequestException {
		log.info("runReport reportRun = " + request);
		try {
			OutputStream oStream = response.getOutputStream();
			IReportRunnable design = getRunnableReportDesign(request);
			// Run Reports will only do a RunAndRender
			final IRunAndRenderTask rrTask = (IRunAndRenderTask) engineService.getEngine().createRunAndRenderTask(design);
			// TODO Does not make sense 
			final Map<String, Object> appContext = rrTask.getAppContext();
			rrTask.setAppContext(appContext);

			configureParameters(request, design, rrTask);

			log.info("getRenderOptions");
			final String format = request.format;
			RenderOption options = configureRenderOptions(format);
			response.setContentType(getMediaType(format).toString());
			response.setHeader("Content-Disposition", "attachment;filename=" + request.getOutputName() + "." + format);

			options.setOutputStream(oStream);

			rrTask.setRenderOption(options);
			log.info("run-and-render report");
			try {
				rrTask.run();
			} catch (final UnsupportedFormatException e) {
				throw new BadRequestException(406, "Unsupported output format");
			} catch (final Exception e) {
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException".equals(e.getClass().getName())) {
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

		} catch (IllegalAccessException | InvocationTargetException | IOException | RunnerException | BadRequestException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error("Failure to Run Report: " + e1.getMessage());

		}

	}

}
