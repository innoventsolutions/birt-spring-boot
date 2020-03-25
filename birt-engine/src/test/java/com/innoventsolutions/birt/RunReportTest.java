/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BirtEngineApplication.class)
@WebAppConfiguration
@Slf4j
public class RunReportTest extends BaseTest {
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
	@Autowired
	private WebApplicationContext context;
	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
	}

	@Test
	public void testRun() throws Exception {
		final ExecuteRequest requestObject = ExecuteRequest.builder()
				.designFile(PARAM_TEST_RPTDESIGN)
				.format("pdf")
				.parameters(PARAM_MAP_1)
				.outputName("test_out_1")
				.build();
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testRunReport request = " + requestString);

		final MvcResult statusResult = this.mockMvc
				.perform(get("/runReport").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("runReport", requestFields(
					fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
					fieldWithPath("format").description(FORMAT_DESCRIPTION),
					fieldWithPath("outputName").optional().type(JsonFieldType.STRING)
							.description(PARAMETERS_DESCRIPTION),
					fieldWithPath("wrapError").optional().type(JsonFieldType.BOOLEAN).description(WRAP_ERROR_DESCRIPTION),
					subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
										.description(PARAMETERS_DESCRIPTION))))
				.andDo(MvcResult::getAsyncResult)
				.andReturn();
		System.out.println(statusResult);
	}

	@Test
	public void testMissingParameter() throws Exception {
		final ExecuteRequest requestObject = ExecuteRequest.builder()
			.designFile(PARAM_TEST_RPTDESIGN)
			.format("pdf")
			.build();
		final Map<String, Object> bad_param = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;

			{
				// required, not including should force an error
				// put("paramString", "String Val");
				put("paramDate", "2010-05-05");
				put("paramInteger", 1111);
				put("paramDecimal", 999.888);
			}
		};
		requestObject.setParameters(bad_param);
		requestObject.setOutputName("failure_out");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.error("testMissingParameter request = " + requestString + " " + Thread.currentThread());

		final MvcResult statusResult = this.mockMvc
				.perform(get("/runReport")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestString)
				.accept(MediaType.APPLICATION_JSON))
				.andDo(MvcResult::getAsyncResult)
				.andExpect(status().is(400))
				.andReturn();

		System.out.println(statusResult);

	}

}
