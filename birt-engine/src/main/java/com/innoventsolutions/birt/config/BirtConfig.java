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

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "birt.runner")
public class BirtConfig {
	@NotBlank
	private File workspace = new File("../birt-engine-workspace");
	private File designDir = new File(workspace, "rptdesign");
	// where log files are sent from the report engine
	private File loggingDir = new File(workspace, "log");
	// any temp or long term file generated from running a rptdesign (.rptdocument,
	// .pdf, .html,...)
	private File outputDir = new File(workspace, "output");
	// any resource file including .rptlibrary, .css, .js, .jar(pojos), data files
	private File resourceDir = new File(workspace, "resources");
	// jar files for event handlers
	private File scriptLibDir = new File(workspace, "lib");
	// Location for images
	private String baseImageURL = new File(workspace, "images").getAbsolutePath();
	private String reportFormat = "pdf";
	private int threadCount = 1;
	// true if using commercial libraries
	private boolean isActuate = false;
	// Required when using commercial libraries
	private File birtRuntimeHome = null;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BIRTConfig: Workspace Loc = ").append(workspace.getAbsolutePath());
		sb.append(" BirtRuntimeHome = ").append(birtRuntimeHome);
		return sb.toString();
	}
}
