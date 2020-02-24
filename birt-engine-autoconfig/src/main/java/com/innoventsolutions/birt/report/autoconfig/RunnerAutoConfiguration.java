package com.innoventsolutions.birt.report.autoconfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innoventsolutions.birt.report.exception.RunnerException;
import com.innoventsolutions.birt.report.service.ConfigService;
import com.innoventsolutions.birt.report.service.RunnerService;

@Configuration
@ConditionalOnClass(RunnerService.class)
@EnableConfigurationProperties(RunnerProperties.class)
public class RunnerAutoConfiguration {
	@Autowired
	private RunnerProperties runnerProperties;

	@Bean
	@ConditionalOnMissingBean
	public ConfigService runnerConfig() {
		// String userHome = System.getProperty("user.home");
		final ConfigService config = new ConfigService();
		config.setOutputDirectory(runnerProperties.getOutputDirectory());
		config.setWorkspace(runnerProperties.getWorkspace());
		config.setBirtRuntimeHome(runnerProperties.getBirtRuntimeHome());
		config.setResourcePath(runnerProperties.getResourcePath());
		config.setScriptLib(runnerProperties.getScriptLib());
		config.setReportFormat(runnerProperties.getReportFormat());
		config.setBaseImageURL(runnerProperties.getBaseImageURL());
		config.setLoggingPropertiesFile(runnerProperties.getLoggingPropertiesFile());
		config.setLoggingDir(runnerProperties.getLoggingDir());
		config.setThreadCount(runnerProperties.getThreadCount());
		config.setActuate(runnerProperties.isActuate());
		config.setUnsecuredDesignFilePattern(runnerProperties.getUnsecuredDesignFilePattern());
		config.setUnsecuredOperationPattern(runnerProperties.getUnsecuredOperationPattern());
		return config;
	}

	@Bean
	@ConditionalOnMissingBean
	public RunnerService runner(final ConfigService runnerConfig) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, RunnerException {
		return new RunnerService(runnerConfig);
	}
}
