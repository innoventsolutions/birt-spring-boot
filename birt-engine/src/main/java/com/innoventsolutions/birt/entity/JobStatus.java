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

import lombok.Data;

/**
 * Entity object containing the jobID that is to be looked up or fetched.
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
@Data
public class JobStatus {
	private String jobid;

}
