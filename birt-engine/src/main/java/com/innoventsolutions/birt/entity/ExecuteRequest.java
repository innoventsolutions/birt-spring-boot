/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.entity;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The report run request.
 *
 * @author Steve Schafer, Innovent Solutions Inc.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteRequest {
	public @NotNull String designFile;
	public @NotNull String outputName;
	public @NotNull String format;
	public @NotNull Map<String, Object> parameters;

	// Don't use Lombok want to have a empty parameter string
	public ExecuteRequest(final String designFile, final String nameForHumans, final String format) {
		this.designFile = designFile;
		this.outputName = nameForHumans;
		this.format = format;
		this.parameters = new HashMap<String, Object>();
	}

}
