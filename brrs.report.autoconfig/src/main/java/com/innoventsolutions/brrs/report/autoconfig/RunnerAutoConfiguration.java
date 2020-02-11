package com.innoventsolutions.brrs.report.autoconfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.birt.core.exception.BirtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innoventsolutions.brrs.report.service.ConfigService;
import com.innoventsolutions.brrs.report.service.RunnerService;

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
		ConfigService config = new ConfigService();
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
	public RunnerService runner(ConfigService runnerConfig) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException, BirtException {
		return new RunnerService(runnerConfig);
	}
}
