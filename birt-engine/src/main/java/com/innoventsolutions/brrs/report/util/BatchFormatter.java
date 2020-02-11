/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brrs.report.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BatchFormatter extends Formatter {
	@Override
	public String format(final LogRecord record) {
		final DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		final StringBuffer sb = new StringBuffer();
		final Date dt = new Date(record.getMillis());
		// Get the level name and add it to the buffer
		sb.append(sdf.format(dt)).append(" ").append(record.getMessage()).append("\n");
		final Throwable exception = record.getThrown();
		if (exception != null) {
			sb.append("Exception: ").append(exception.getMessage()).append("\n");
			final StackTraceElement[] stes = exception.getStackTrace();
			if (stes != null) {
				for (final StackTraceElement ste : stes) {
					sb.append("  at ").append(ste.toString()).append("\n");
				}
			}
		}
		return sb.toString();
	}
}
