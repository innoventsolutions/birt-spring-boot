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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "birt.runner")
public class BirtConfig {
	//@NotBlank 
	@Value("birt.runner.workspace:../birt-spring-boot-starter/birt-engine-workspace")
	private File workspace;
	@Value("${birt.runner.workspace}/rptdesign")
	private File designDir;

	// where log files are sent from the report engine
	@Value("${birt.runner.workspace}/log")
	private File loggingDir;
	// any temp or long term file generated from running a rptdesign (.rptdocument,
	// .pdf, .html,...)
	@Value("${birt.runner.workspace}/output")
	private File outputDir;
	// any resource file including .rptlibrary, .css, .js, .jar(pojos), data files
	@Value("${birt.runner.workspace}/resources")
	private File resourceDir;
	// jar files for event handlers
	@Value("${birt.runner.workspace}/lib")
	private File scriptLibDir;
	// Location for images
	@Value("${birt.runner.workspace}/images")
	private String baseImageURL;
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
