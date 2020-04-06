/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Common requests object shared by RunReport and SubmitJob
 *  Parameters should be a comma separated string of Name/Value pairs
 *
 * @author Scott Rosenbaum / Steve Schafer
 */
@Getter @Setter @Builder @ToString
@AllArgsConstructor @NoArgsConstructor
public class ExecuteRequest {
	private String designFile;
	private String outputName;
	@Builder.Default private String format = "HTML";
	@Builder.Default private Map<String, Object> parameters = new HashMap<String, Object>();
	@Builder.Default private Boolean wrapError = true;

}
