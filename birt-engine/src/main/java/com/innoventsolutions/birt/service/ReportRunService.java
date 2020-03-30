/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.impl.ParameterValidationException;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BirtStarterException;
import com.innoventsolutions.birt.exception.BirtStarterException.BirtErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportRunService extends BaseReportService {

	public ReportRunService(final BirtEngineService engineService) {
		super(engineService);
		log.info("Start RunService");
	}

	@SuppressWarnings("unchecked")
	public void execute(final ExecuteRequest request, HttpServletResponse response) throws BirtStarterException {
		log.info("runReport reportRun = " + request);
		IRunAndRenderTask rrTask = null;
		try {

			final IReportRunnable design = getRunnableReportDesign(request);
			rrTask = engineService.getEngine().createRunAndRenderTask(design);
			// TODO Does not make sense
			final Map<String, Object> appContext = rrTask.getAppContext();
			rrTask.setAppContext(appContext);

			configureParameters(request, design, rrTask);

			log.info("getRenderOptions");
			final String format = request.getFormat();
			final RenderOption options = configureRenderOptions(format);
			options.setOutputStream(response.getOutputStream());
			rrTask.setRenderOption(options);
			log.info("run-and-render report");
			rrTask.run();
			final List<EngineException> errors = rrTask.getErrors();
			if (errors != null && errors.size() > 0) {
				throw new BirtStarterException(BirtErrorCode.RUNANDRENDER_TASK, errors);
			}
			rrTask.close();
		} catch (final IOException e) {
			throw new BirtStarterException(BirtErrorCode.DESIGN_FILE_LOCATION, "Failure to run report (design file)", e);
		} catch (EngineException e) {
			if (e instanceof ParameterValidationException) {
				throw new BirtStarterException(BirtErrorCode.PARAMETER_VALIDATION, "Failure to run report (parameter)" , e);
			} else {
				throw new BirtStarterException(BirtErrorCode.RUNANDRENDER_TASK, "Failure to run report", e);
			}
		} finally {
			if (rrTask != null)
				rrTask.close();
		}

	}

}
