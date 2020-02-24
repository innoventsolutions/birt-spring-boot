/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// having 2 spring boot applications makes mvn package fail
@SpringBootApplication
public class BirtEngineApplication {
	public static void main(final String[] args) {
		SpringApplication.run(BirtEngineApplication.class, args);
	}
}
