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

import javax.validation.constraints.NotNull;

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
	private @NotNull String designFile;
	private @NotNull String outputName;
	private @NotNull String format;
	private @NotNull Map<String, Object> parameters;
	private Boolean wrapError;

	// Don't use Lombok want to have a empty parameter string
	public ExecuteRequest(final String designFile, final String outputName, final String format, Boolean wrapError) {
		this.designFile = designFile;
		this.outputName = outputName;
		this.format = format;
		this.parameters = new HashMap<String, Object>();
		if (wrapError == null)
			wrapError = false;
		this.wrapError = wrapError;
	}
}
