/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brrs.report;

import java.util.Map;
import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The immutable report run request.
 *
 * @author Steve Schafer, Innovent Solutions Inc.
 */
@AllArgsConstructor
@Getter
@ToString
public class ReportRun {
	public final @NotNull String designFile;
	public final String nameForHumans;
	public final @NotNull String format;
	public final @NotNull String outputFile;
	public final boolean runThenRender;
	public final Map<String, Object> parameters;

}
