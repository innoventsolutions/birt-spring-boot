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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.innoventsolutions")
public class BirtEngineApplication {

	public static void main(final String[] args) {
		SpringApplication.run(BirtEngineApplication.class, args);
	}

	@Bean(name = "submitJobExecutor")
	public ExecutorService taskExecutor() {
		final ExecutorService executor = Executors.newFixedThreadPool(10);

		return executor;
	}
}
