/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.config;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirtConfig {
	// @NotBlank
	private File workspace;
	private File designDir;
	private File loggingDir;
	private File outputDir;
	private File resourceDir;
	private File scriptLibDir;
	private String baseImageURL;
	private String reportFormat = "pdf";
	private boolean isActuate = false;
	private File birtRuntimeHome = null;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BIRTConfig: Workspace Loc = ").append(workspace.getAbsolutePath());
		sb.append(" BirtRuntimeHome = ").append(birtRuntimeHome);
		return sb.toString();
	}
}
