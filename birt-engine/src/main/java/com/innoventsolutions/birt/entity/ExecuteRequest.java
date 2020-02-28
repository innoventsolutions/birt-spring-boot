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
import lombok.ToString;

/**
 * The immutable report run request.
 *
 * @author Steve Schafer, Innovent Solutions Inc.
 */
@Getter
@ToString
@AllArgsConstructor
public class ExecuteRequest {
	public final @NotNull String designFile;
	public final String nameForHumans;
	public final @NotNull String format;
	public final @NotNull Map<String, Object> parameters;

	// Don't use Lombok want to have a empty parameter string 
	public ExecuteRequest(String designFile, String nameForHumans, String format) {
		this.designFile = designFile;
		this.nameForHumans = nameForHumans;
		this.format = format;
		this.parameters = new HashMap<String, Object>();
	}

}
