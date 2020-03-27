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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.exception.BirtStarterException;
import com.innoventsolutions.birt.exception.BirtStarterException.BirtErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BirtEngineService {

	@Autowired
	private BirtConfig birtConfig;
	private IReportEngine engine = null;

	@Autowired
	public BirtEngineService() {
	}

	@PostConstruct
	public IReportEngine getEngine() throws BirtStarterException {
		if (engine == null) {
			log.info("Instantiate Report Engine");
			this.engine = getReportEngine();
		}
		return engine;
	}

	// Pass up required configuration parameters, does it make sense to just have
	// the calling classes use the configuration directly?
	public String getBaseImageURL() {
		return birtConfig.getBaseImageURL();
	}

	public File getOutputDir() {
		return birtConfig.getOutputDir();
	}

	public File getWorkspace() {
		return birtConfig.getWorkspace();
	}

	public File getDesignDir() {
		return birtConfig.getDesignDir();
	}

	private IReportEngine getReportEngine() throws BirtStarterException {

		log.info("getReportEngine");
		final EngineConfig config = new EngineConfig();
		log.info(birtConfig.toString());
		if (birtConfig.getBirtRuntimeHome() != null) {
			final String birtHome = birtConfig.getBirtRuntimeHome().getAbsolutePath();
			if (birtConfig.isActuate()) {
				config.setBIRTHome(birtHome);
			} else {
				config.setEngineHome(birtHome);
			}
		}
		if (birtConfig.getResourceDir() != null) {
			final File workspaceDir = new File(birtConfig.getWorkspace().getAbsolutePath());
			config.setResourcePath(workspaceDir.getAbsolutePath());
		}
		final String scriptlibFileNames = getScriptLibFileNames();
		if (scriptlibFileNames != null) {
			config.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, scriptlibFileNames);
		}
		configureLogging(config);
		IReportEngine birtEngine =birtConfig.isActuate() ? getActuateReportEngine(config) : getReportEngine(config);
		
		// Add test and warning if no report design folder is present 
		File designDir = birtConfig.getDesignDir();
		if (!designDir.exists() || !designDir.isDirectory()) {
			StringBuffer sb = new StringBuffer();
			sb.append("Design Dir does not exist or is not a folder. ");
			sb.append("\nWorkspace Location: ").append(birtConfig.getWorkspace().getAbsolutePath());
			sb.append((birtConfig.getWorkspace().exists()) ? "(exists)" : " (does not exist)");
			String ddd = ( birtConfig.getDesignDir() == null) ? "not set" : birtConfig.getDesignDir().getAbsolutePath(); 
			sb.append("\nDesign Dir: ").append(ddd);
			sb.append((birtConfig.getDesignDir().exists()) ? "(exists)" : " (does not exist)");
			sb.append("\nCheck application.properties file");
			log.warn(sb.toString());
		}
		return birtEngine;
	}

	private void configureLogging(final EngineConfig config) throws BirtStarterException {
		// control debug of BIRT components.
		final File loggingDirFile = birtConfig.getLoggingDir() == null ? new File("./log") : birtConfig.getLoggingDir();
		if (!loggingDirFile.exists()) {
			loggingDirFile.mkdirs();
		}
		config.setLogConfig(loggingDirFile.getAbsolutePath(), Level.WARNING);
	}

	private static IReportEngine getReportEngine(final EngineConfig config) throws BirtStarterException {
		log.info("before Platform startup");
		try {
			Platform.startup(config);
		} catch (final BirtException e) {
			throw new BirtStarterException(BirtErrorCode.PLATFORM_START, "Failed to start platform", e);
		}
		log.info("after Platform startup");
		final IReportEngineFactory factory = (IReportEngineFactory) Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		if (factory == null) {
			log.error("Could not create report engine factory");
			throw new BirtStarterException(BirtErrorCode.PLATFORM_START, "Failed to create Factory");
		}
		final IReportEngine engine = factory.createReportEngine(config);
		if (engine == null) {
			log.error("Could not create report engine");
			throw new BirtStarterException(BirtErrorCode.PLATFORM_START, "Failed to create Engine");
		}
		return engine;
	}

	private static IReportEngine getActuateReportEngine(final EngineConfig config) throws BirtStarterException {
		try {
			Platform.startup(config);
		} catch (final BirtException e) {
			throw new BirtStarterException(BirtErrorCode.PLATFORM_START, "Failed to start platform", e);
		}
		final Object factoryObjectForReflection = Platform
				.createFactoryObject("com.actuate.birt.report.engine.ActuateReportEngineFactory"
				/* IActuateReportEngineFactory. EXTENSION_ACTUATE_REPORT_ENGINE_FACTORY */
				);
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
				try {
					reportEngine = (IReportEngine) m.invoke(factoryObjectForReflection, config);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new BirtStarterException(BirtErrorCode.PLATFORM_START, "Failed to create report engine", e);
				}
			}
		}
		return reportEngine;
	}

	/*
	 * The engine needs to see a list of each jar file concatenated as a string
	 * using the standard file system separator to divide the files
	 */
	private String getScriptLibFileNames() {
		if (birtConfig.getScriptLibDir() == null) {
			return null;
		}
		if (!birtConfig.getScriptLibDir().exists()) {
			birtConfig.getScriptLibDir().mkdirs();
		}
		final File[] files = birtConfig.getScriptLibDir().listFiles(new JarFilter());
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

	@SuppressWarnings("deprecation")
	public void shutdown() {
		// there is really no place this can be done
		log.info("engine shutdown");
		// TODO Figure out how to shutdown engine
		engine.shutdown();
	}

}
