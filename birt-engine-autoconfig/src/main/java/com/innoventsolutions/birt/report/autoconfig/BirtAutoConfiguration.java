/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.report.autoconfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.controller.RunController;
import com.innoventsolutions.birt.controller.SubmitController;
import com.innoventsolutions.birt.service.BirtEngineService;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.service.SubmitJobService;

@Configuration
@EnableConfigurationProperties(BirtConfig.class)
public class BirtAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public BirtEngineService engineService() {
		return new BirtEngineService();
	}

	@Bean
	@ConditionalOnMissingBean
	public ReportRunService runService() {
		return new ReportRunService();
	}

	@Bean
	@ConditionalOnMissingBean
	public SubmitJobService submitService() {
		return new SubmitJobService();
	}

	@ConditionalOnMissingBean
	@Bean(name = "submitJobExecutor")
	public ExecutorService taskExecutor() {
		final ExecutorService executor = Executors.newFixedThreadPool(10);

		return executor;
	}

	@Bean
	@ConditionalOnMissingBean
	public RunController runController() {
		return new RunController();
	}

	@Bean
	@ConditionalOnMissingBean
	public SubmitController submitController() {
		return new SubmitController();
	}
/*
	@Bean
	@ConditionalOnMissingBean(name = "runExecutor")
	public BirtAsyncConfiguration asyncConfiguration() {
		return new BirtAsyncConfiguration();
	}
*/
}
