package com.innoventsolutions.birt;

import java.util.HashMap;
import java.util.Map;

import org.springframework.test.web.servlet.MockMvc;

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
	protected MockMvc mockMvc;

	public BaseTest() {
		super();
	}

}