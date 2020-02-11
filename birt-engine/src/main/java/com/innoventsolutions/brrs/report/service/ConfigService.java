/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brrs.report.service;

import java.io.File;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigService {
	public File outputDirectory;
	public File workspace;
	public File birtRuntimeHome;
	public File resourcePath;
	public File scriptLib;
	public File loggingPropertiesFile;
	public File loggingDir;
	public String reportFormat;
	public String baseImageURL;
	public int threadCount;
	public boolean isActuate;
	public Pattern unsecuredDesignFilePattern;
	public Pattern unsecuredOperationPattern;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
