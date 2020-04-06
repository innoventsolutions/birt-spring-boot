/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innoventsolutions.birt.controller.RunController;
import com.innoventsolutions.birt.controller.SubmitController;
import com.innoventsolutions.birt.service.BirtEngineService;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.service.SubmitJobService;

/**
 * AutoConfigure class instantiate BIRT-Engine Service and 
 * the the two BIRT Controller classes.
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Configuration
@EnableConfigurationProperties(BirtProperties.class)
public class BirtAutoConfiguration {
	@Autowired
	BirtProperties birtProperties;

	@Bean
	@ConditionalOnMissingBean
	public BirtEngineService engineService() {

		return new BirtEngineService(birtProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public ReportRunService runService() {
		// TODO use the engineService bean
		return new ReportRunService(engineService());
	}

	@Bean
	@ConditionalOnMissingBean
	public SubmitJobService submitService() {
		final ForkJoinPool fjp = new ForkJoinPool(birtProperties.getSubmitJobPoolSize());
		return new SubmitJobService(engineService(), fjp);
	}

	@ConditionalOnMissingBean
	@Bean(name = "submitJobExecutor")
	public ExecutorService taskExecutor() {
		final ExecutorService executor = Executors.newFixedThreadPool(birtProperties.getRunReportPoolSize());
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
}
