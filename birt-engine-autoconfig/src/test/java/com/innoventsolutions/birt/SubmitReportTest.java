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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.JobStatus;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BirtEngineApplication.class)
@WebAppConfiguration
@Slf4j
public class SubmitReportTest extends BaseTest {
	private static final Object JOB_ID_DESCRIPTION = "The job id returned from /submitJob";
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
	}

	@Test
	public void testSubmit() throws Exception {
		final ExecuteRequest requestObject = ExecuteRequest.builder().designFile(PARAM_TEST_RPTDESIGN).format("pdf")
				.parameters(PARAM_MAP_1).outputName("test_submit1_out_1").build();
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testSubmit request = " + requestString);

		final MvcResult result = this.mockMvc
				.perform(get("/submitJob").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("submitJob", requestFields(
						fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("format").description(FORMAT_DESCRIPTION),
						fieldWithPath("outputName").optional().type(JsonFieldType.STRING)
								.description(PARAMETERS_DESCRIPTION),
						fieldWithPath("wrapError").optional().type(JsonFieldType.BOOLEAN).description(
								"If true, generate a JSON error response.  If false, generate a simple error response that will generate a whitelabel error page."),
						subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION))))

				.andReturn();
		System.out.println(result);
	}

	@Test
	public void testSubmitBadDesignFile() throws Exception {
		final ExecuteRequest submitRequestObject = ExecuteRequest.builder().designFile("foobar").format("pdf")
				.parameters(PARAM_MAP_1).outputName("test_submit1_out_1").build();
		final ObjectMapper mapper = new ObjectMapper();
		final String submitRequestString = mapper.writeValueAsString(submitRequestObject);
		log.info("testSubmitBadDesignFile request = " + submitRequestString);

		final MvcResult submitResult = this.mockMvc.perform(get("/submitJob").contentType(MediaType.APPLICATION_JSON)
				.content(submitRequestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andReturn();
		System.out.println(submitResult);
		final JobStatus waitForJobRequestObject = new JobStatus();
		waitForJobRequestObject.setJobid(getJobId(submitResult));
		final String waitForJobRequestString = mapper.writeValueAsString(waitForJobRequestObject);
		log.info("testGetJobInfo request = " + waitForJobRequestString);

		final MvcResult waitForJobResult = this.mockMvc
				.perform(get("/waitForJob").contentType(MediaType.APPLICATION_JSON).content(waitForJobRequestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(404)).andReturn();
		System.out.println(waitForJobResult);
	}

	@Test
	public void testSubmitBadFormat() throws Exception {
		final String jobId = submit("foobar");
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid(jobId);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testGetJobInfo request = " + requestString);

		final MvcResult result = this.mockMvc.perform(get("/waitForJob").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().is(415)).andReturn();
		System.out.println(result);
	}

	@Test
	public void testGetJobInfo() throws Exception {
		final String jobId = submit("pdf");
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid(jobId);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testGetJobInfo request = " + requestString);

		final MvcResult result = this.mockMvc
				.perform(get("/getJobInfo").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("getJobInfo", requestFields(fieldWithPath("jobid").description(JOB_ID_DESCRIPTION))))
				.andReturn();
		final MockHttpServletResponse httpServletResponse = result.getResponse();
		Assert.assertTrue(httpServletResponse.getContentType().startsWith("application/json"));
		final String jsonString = httpServletResponse.getContentAsString();
		log.info("getJobInfo response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> getJobInfoResponse = mapper.readValue(jsonString, Map.class);
		// {cancelled=false, completedExceptionally=false, numberOfDependents=0,
		// done=true}
		final Object cancelled = getJobInfoResponse.get("cancelled");
		Assert.assertNotNull(cancelled);
		Assert.assertTrue(Boolean.FALSE.equals(cancelled));
		final Object completedExceptionally = getJobInfoResponse.get("completedExceptionally");
		Assert.assertNotNull(completedExceptionally);
		Assert.assertTrue(Boolean.FALSE.equals(completedExceptionally));
		final Object numberOfDependents = getJobInfoResponse.get("numberOfDependents");
		Assert.assertNotNull(numberOfDependents);
		Assert.assertTrue(Integer.valueOf(0).equals(numberOfDependents));
		final Object done = getJobInfoResponse.get("done");
		Assert.assertNotNull(done);
		log.info("getJobInfo done = " + done);
	}

	@Test
	public void testGetJobInfoInvalidId() throws Exception {
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid("foobar");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testGetJobInfo request = " + requestString);

		final MvcResult result = this.mockMvc.perform(get("/getJobInfo").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().is(404)).andReturn();
		System.out.println(result);
	}

	@Test
	public void testWaitForJob() throws Exception {
		final String jobId = submit("pdf");
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid(jobId);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testWaitForJob request = " + requestString);

		final MvcResult result = this.mockMvc
				.perform(get("/waitForJob").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("waitForJob", requestFields(fieldWithPath("jobid").description(JOB_ID_DESCRIPTION))))
				.andReturn();
		final MockHttpServletResponse httpServletResponse = result.getResponse();
		Assert.assertTrue(httpServletResponse.getContentType().startsWith("application/json"));
		final String jsonString = httpServletResponse.getContentAsString();
		log.info("testWaitForJob response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String responseJobId = submitResponse.get("jobid");
		Assert.assertNotNull(responseJobId);
		Assert.assertEquals(jobId, responseJobId);
		final String status = submitResponse.get("status");
		Assert.assertNotNull(status);
		System.out.println(status);
		Assert.assertEquals(StatusEnum.COMPLETE.toString(), status);
	}

	@Test
	public void testWaitForJobInvalidId() throws Exception {
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid("foobar");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testWaitForJob request = " + requestString);

		final MvcResult result = this.mockMvc.perform(get("/waitForJob").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().is(404)).andReturn();
		System.out.println(result);
	}

	@Test
	public void testDeleteJob() throws Exception {
		final String jobId = submit("pdf");
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid(jobId);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("/deleteJob request = " + requestString);

		final MvcResult result = this.mockMvc
				.perform(delete("/deleteJob").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("deleteJob", requestFields(fieldWithPath("jobid").description(JOB_ID_DESCRIPTION))))
				.andReturn();
		final MockHttpServletResponse httpServletResponse = result.getResponse();
		Assert.assertTrue(httpServletResponse.getContentType().startsWith("application/json"));
		final String jsonString = httpServletResponse.getContentAsString();
		log.info("/deleteJob response = " + jsonString);
		final Boolean response = mapper.readValue(jsonString, Boolean.class);
		System.out.println(response);
	}

	@Test
	public void testDeleteJobInvalidId() throws Exception {
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid("foobar");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testDeleteJob request = " + requestString);

		final MvcResult result = this.mockMvc.perform(delete("/deleteJob").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().is(404)).andReturn();
		System.out.println(result);
	}

	@Test
	public void testGetReport() throws Exception {
		final String jobId = submit("pdf");
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid(jobId);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testGetReport request = " + requestString);

		final MvcResult result = this.mockMvc.perform(get("/getReport").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		final MockHttpServletResponse httpServletResponse = result.getResponse();
		final String contentType = httpServletResponse.getContentType();
		Assert.assertEquals("application/pdf", contentType);
		final String content = httpServletResponse.getContentAsString();
		Assert.assertTrue(content.startsWith("%PDF-1.5"));
		log.info("getReport done");
	}

	@Test
	public void testGetReportInvalidId() throws Exception {
		final JobStatus requestObject = new JobStatus();
		requestObject.setJobid("foobar");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testGetReport request = " + requestString);

		final MvcResult result = this.mockMvc.perform(get("/getReport").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().is(404)).andReturn();
		System.out.println(result);
	}

	/*
	 * Used for testing endpoints that require a job
	 */
	private String submit(final String format) throws Exception {
		final ExecuteRequest requestObject = ExecuteRequest.builder().designFile(PARAM_TEST_RPTDESIGN).format(format)
				.parameters(PARAM_MAP_1).outputName("test_submit_out_1").build();

		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("submit request = " + requestString);
		final MvcResult result = this.mockMvc.perform(get("/submitJob").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		return getJobId(result);
	}

	private static String getJobId(final MvcResult result)
			throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("submit response = " + jsonString);
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String jobId = submitResponse.get("jobid");
		log.info("submit response jobId = " + jobId);
		final String exceptionString = submitResponse.get("exceptionString");
		log.info("submit response exceptionString = " + exceptionString);
		Assert.assertFalse("Exception string is not null and not blank: " + exceptionString,
				exceptionString != null && exceptionString.trim().length() > 0);
		Assert.assertNotNull("Job ID is null", jobId);
		return jobId;
	}
}
