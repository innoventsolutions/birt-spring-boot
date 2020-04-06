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

/**
 * Configuration for the BIRT Engine.
 *  By convention, the Workspace will be the parent directory for all other content
 *   The directories are considered roots for other content.
 *   In most cases, the resourceDir will be the same as the Workspace, e.g. the typical folder structure will be
 *   
 * Workspace (Resource)
 * - designDir
 * - dataDir
 * - propertyDir
 * - jsDir
 * - scriptLibDir
 * - rptlibrary
 * - ...
 * 
 * Should be coordinated with users use of Resource Home in BIRT Designer
 *  (Resource Home is under Window -> Preferences -> Report Design -> Resources)
 *   
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Getter @Setter
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
