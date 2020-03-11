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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.UnsupportedFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;
import com.innoventsolutions.birt.exception.BadRequestException;
import com.innoventsolutions.birt.exception.RunnerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubmitJobService extends BaseReportService {

	@Autowired
	ExecutorService executorService;

	ForkJoinPool submitPool = new ForkJoinPool(10);

	@Autowired
	public SubmitJobService() {
		log.info("Start RunService");
	}

	@Async
	public CompletableFuture<SubmitResponse> executeRunThenRender(final SubmitResponse submitResponse,
			final HttpServletResponse httpResponse) {
		log.info("RunThenRender: " + submitResponse.getRequest().getOutputName());

		// to use Fork/join change the executor to submitPool (or opposite)
		final CompletableFuture<SubmitResponse> runThenRender = CompletableFuture
				.supplyAsync((() -> executeRun(submitResponse, httpResponse)), executorService)
				.thenApply(l -> executeRender(submitResponse, httpResponse));

		return runThenRender;
	}

	public FileInputStream getReport(final SubmitResponse job) throws FileNotFoundException {

		final File f = new File(engineService.getOutputDir(), job.getOutFileName());
		return new FileInputStream(f);

	}

	@SuppressWarnings("unchecked")
	public SubmitResponse executeRender(final SubmitResponse submitResponse, final HttpServletResponse response) {

		submitResponse.setStatus(StatusEnum.RUN);
		submitResponse.setRenderBegin(new Date());
		log.info("submitJob (Render) = " + submitResponse.getRequest() + "[" + submitResponse.getJobid() + "]");
		final ExecuteRequest request = submitResponse.getRequest();

		IReportDocument rptdoc = null;
		try {
			final IReportEngine engine = engineService.getEngine();
			final File rptDocFile = new File(engineService.getOutputDir(), submitResponse.getRptDocName());
			final File outputFile = new File(engineService.getOutputDir(), submitResponse.getOutFileName());
			rptdoc = engine.openReportDocument(rptDocFile.getAbsolutePath());
			final IRenderTask renderTask = engine.createRenderTask(rptdoc);
			// TODO Does not make sense
			// final Map<String, Object> appContext = renderTask.getAppContext();

			log.info("Rendering doc: " + request.getOutputName() + " to " + request.getFormat());

			final String format = request.format;
			final RenderOption options = configureRenderOptions(format);

			outputFile.getParentFile().mkdirs();
			options.setOutputFileName(outputFile.getAbsolutePath());
			options.setOutputFormat(format);
			options.setOutputFileName(outputFile.getAbsolutePath());
			renderTask.setRenderOption(options);

			try {
				renderTask.render();
			} catch (final UnsupportedFormatException e) {
				throw new BadRequestException(406, "Unsupported output format");
			} catch (final Exception e) {
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException"
						.equals(e.getClass().getName())) {
					throw new BadRequestException(406, e.getMessage());
				}
				throw new RunnerException("Run Task failed", e);
			}
			final List<Exception> exceptions = new ArrayList<>();
			final List<EngineException> errors = renderTask.getErrors();
			if (errors != null && errors.size() > 0) {
				for (final EngineException exception : errors) {
					exceptions.add(exception);
				}
			}

			rptdoc.close();
		} catch (final BadRequestException e1) {
			try {
				response.sendError(e1.getCode(), e1.getReason());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IllegalAccessException | InvocationTargetException | IOException | RunnerException e1) {
			try {
				response.sendError(500, e1.getMessage());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (final EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (final IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			// Failure to close the report doc will result in a locked file
			if (rptdoc != null) {
				rptdoc.close();
			}
		}

		submitResponse.setRenderFinish(new Date());
		return submitResponse;
	}

	@SuppressWarnings("unchecked")
	public SubmitResponse executeRun(final SubmitResponse submitResponse, final HttpServletResponse response) {
		submitResponse.setStatus(StatusEnum.RENDER);
		submitResponse.setRunBegin(new Date());
		log.info("submitJob (Run) Thread: " + Thread.currentThread() + submitResponse.getRequest());

		try {
			final IReportRunnable design = getRunnableReportDesign(submitResponse.getRequest());
			// Run Reports will only do a RunAndRender
			final IRunTask rTask = engineService.getEngine().createRunTask(design);
			// TODO Does not make sense
			final Map<String, Object> appContext = rTask.getAppContext();
			rTask.setAppContext(appContext);
			configureParameters(submitResponse.getRequest(), design, rTask);

			final File rptDocFile = new File(engineService.getOutputDir(), submitResponse.getRptDocName());
			final String rptDoc = rptDocFile.getAbsolutePath();
			log.info("Creating rpt doc: " + rptDoc);
			rTask.setReportDocument(rptDoc);

			try {
				rTask.run();
			} catch (final UnsupportedFormatException e) {
				throw new BadRequestException(406, "Unsupported output format");
			} catch (final Exception e) {
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException"
						.equals(e.getClass().getName())) {
					throw new BadRequestException(406, e.getMessage());
				}
				throw new RunnerException("Run Task failed", e);
			}
			final List<Exception> exceptions = new ArrayList<>();
			final List<EngineException> errors = rTask.getErrors();
			if (errors != null && errors.size() > 0) {
				for (final EngineException exception : errors) {
					exceptions.add(exception);
				}

			}
		} catch (final BadRequestException e1) {
			try {
				response.sendError(e1.getCode(), e1.getReason());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IllegalAccessException | InvocationTargetException | IOException | RunnerException e1) {
			try {
				response.sendError(500, e1.getMessage());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		submitResponse.setRunFinish(new Date());
		submitResponse.setStatus(StatusEnum.COMPLETE);

		return submitResponse;
	}

}
