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
	private final Boolean mailSuccess;
	private final Boolean mailFailure;
	private final String mailTo;
	private final String mailCc;
	private final String mailBcc;
	private final String mailSuccessSubject;
	private final String mailFailureSubject;
	private final String mailSuccessBody;
	private final String mailFailureBody;
	private final Boolean mailAttachReport;
	private final Boolean mailHtml;
}
