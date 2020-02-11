/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brrs.report.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.innoventsolutions.brrs.report.util.BatchFormatter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BirtService {
	private final ConfigService configuration;

	public BirtService(@Autowired final ConfigService configuration) {
		this.configuration = configuration;
	}

	public IReportEngine getReportEngine() throws IOException, BirtException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		log.info("getReportEngine");
		final EngineConfig config = new EngineConfig();
		log.info("birtRuntimeHome = " + configuration.birtRuntimeHome);
		if (configuration.birtRuntimeHome != null) {
			final String birtHome = configuration.birtRuntimeHome.getAbsolutePath();
			if (configuration.isActuate) {
				config.setBIRTHome(birtHome);
			}
			else {
				config.setEngineHome(birtHome);
			}
		}
		if (configuration.resourcePath != null) {
			final String resourceDir = configuration.resourcePath.getAbsolutePath();
			config.setResourcePath(resourceDir);
		}
		final String scriptlibFileNames = getScriptLibFileNames();
		if (scriptlibFileNames != null) {
			config.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, scriptlibFileNames);
		}
		final File loggingProperties = configuration.loggingPropertiesFile;
		LogManager.getLogManager().readConfiguration(new FileInputStream(loggingProperties));
		final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
		final Handler[] handlers = rootLogger.getHandlers();
		for (final Handler handler : handlers) {
			handler.setFormatter(new BatchFormatter());
		}
		// control debug of BIRT components.
		final File loggingDirFile = configuration.loggingDir == null ? new File("./log")
			: configuration.loggingDir;
		if (!loggingDirFile.exists()) {
			loggingDirFile.mkdirs();
		}
		config.setLogConfig(loggingDirFile.getAbsolutePath(), Level.WARNING);
		return configuration.isActuate ? getActuateReportEngine(config) : getReportEngine(config);
	}

	public static IReportEngine getReportEngine(final EngineConfig config) throws BirtException {
		System.out.println("before Platform startup");
		Platform.startup(config);
		System.out.println("after Platform startup");
		final IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(
			IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		if (factory == null) {
			System.out.println("Could not create report engine factory");
			throw new NullPointerException("Could not create report engine factory");
		}
		final IReportEngine engine = factory.createReportEngine(config);
		if (engine == null) {
			System.out.println("Could not create report engine");
			throw new NullPointerException("Could not create report engine");
		}
		return engine;
	}

	public static IReportEngine getActuateReportEngine(final EngineConfig config)
			throws BirtException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Platform.startup(config);
		final Object factoryObjectForReflection = Platform.createFactoryObject(
			"com.actuate.birt.report.engine.ActuateReportEngineFactory" /* IActuateReportEngineFactory.EXTENSION_ACTUATE_REPORT_ENGINE_FACTORY */ );
		// when using the Actuate Report Engine Factory, the return type is
		// not exposed publicly, so you cannot instantiate the factory
		// under normal conditions.
		// but we can use reflection to call the createReportEngine method
		// and get the commercial report engine running as opposed to the
		// open source one, which
		// will give access to all the commercial emitters
		final Class<?> factoryClass = factoryObjectForReflection.getClass();
		final Method[] methods = factoryClass.getDeclaredMethods();
		IReportEngine reportEngine = null;
		for (final Method m : methods) {
			final String name = m.getName();
			m.setAccessible(true);
			if (name.equals("createReportEngine")) {
				reportEngine = (IReportEngine) m.invoke(factoryObjectForReflection, config);
			}
		}
		return reportEngine;
	}

	/*
	 * The engine needs to see a list of each jar file concatenated as a string
	 * using the standard file system separator to divide the files
	 */
	private String getScriptLibFileNames() {
		if (configuration.scriptLib == null) {
			return null;
		}
		if (!configuration.scriptLib.exists()) {
			configuration.scriptLib.mkdirs();
		}
		final File[] files = configuration.scriptLib.listFiles(new JarFilter());
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		final String fileSeparatorString = new String(new char[] { File.pathSeparatorChar });
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				sb.append(sep);
				sep = fileSeparatorString;
				sb.append(files[i].getAbsolutePath());
			}
		}
		return sb.toString();
	}

	private static class JarFilter implements FilenameFilter {
		private final String extension = ".jar";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}
