/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Common static strings used during testing of BIRT Engine
 * 
 * @author Scott Rosenbaum / Steve Schafer
 *
 */
public class BaseTest {

	protected static final String WRAP_ERROR_DESCRIPTION = "If true, generate a JSON error response.  If false, generate a simple error response that will generate a whitelabel error page.";
	protected static final String PARAM_TEST_RPTDESIGN = "param_test.rptdesign";
	protected static final Object DESIGN_FILE_DESCRIPTION = "The full path to the BIRT design file on the server file system";
	protected static final Object FORMAT_DESCRIPTION = "The report output format: HTML, PDF, XLS, or any other format supported by the BIRT engine";
	protected static final Object PARAMETERS_DESCRIPTION = "The parameters in the form {\"name\": value, ...}, where value may be a string, number or boolean for single value parameters or an array of string, number, or boolean for multi-valued parameters.";
	protected static final Map<String, Object> PARAM_MAP_1 = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put("paramString", "String Val");
				put("paramDate", "2010-05-05");
				put("paramInteger", 1111);
				put("paramDecimal", 999.888);
			}
		};
	protected static final String HTML_INTRO = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
	protected static final ResultMatcher APIERROR_SUBERRORS_EMPTY = MockMvcResultMatchers.jsonPath("$.apierror.subErrors").isEmpty();
	protected static final ResultMatcher APIERROR_MSG_RUNFAIL_PARAM = MockMvcResultMatchers.jsonPath("$.apierror.message").value("Failure to run report (parameter)");
	protected static final ResultMatcher APIERROR_MSG_RUNFAIL_DESIGN = MockMvcResultMatchers.jsonPath("$.apierror.message").value("Design file not found fud_fud_fud.rptdesign");
	protected static final ResultMatcher APIERROR_STATUS_BAD_REQUEST = MockMvcResultMatchers.jsonPath("$.apierror.status").value("BAD_REQUEST");
	protected static final ResultMatcher APIERROR_EXISTS = MockMvcResultMatchers.jsonPath("$.apierror").exists();
	protected MockMvc mockMvc;

	public BaseTest() {
		super();
	}

}