/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package sample.birt.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EmailRequest {
	private final Boolean success;
	private final Boolean failure;
	private final String to;
	private final String cc;
	private final String bcc;
	private final String successSubject;
	private final String failureSubject;
	private final String successBody;
	private final String failureBody;
	private final Boolean attachReport;
	private final Boolean html;
	private final Boolean enable = true;
}
