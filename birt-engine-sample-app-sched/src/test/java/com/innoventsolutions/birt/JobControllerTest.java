package com.innoventsolutions.birt;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import sample.birt.BirtSample;
import sample.birt.entity.ScheduleCronRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BirtSample.class)
@WebAppConfiguration
@Slf4j
public class JobControllerTest {
	private static final String PARAM_TEST_RPTDESIGN = "param_test.rptdesign";
	private static final Object DESIGN_FILE_DESCRIPTION = "The full path to the BIRT design file on the server file system";
	private static final Object FORMAT_DESCRIPTION = "The report output format: HTML, PDF, XLS, or any other format supported by the BIRT engine";
	private static final Object PARAMETERS_DESCRIPTION = "The parameters in the form {\"name\": value, ...}, where value may be a string, number or boolean for single value parameters or an array of string, number, or boolean for multi-valued parameters.";
	private static final Object OUTPUT_NAME_DESCRIPTION = "The report output file name";
	private static final Map<String, Object> PARAM_MAP_1 = new HashMap<String, Object>() {
		private static final long serialVersionUID = 1L;
		{
			put("paramString", "String Val");
			put("paramDate", "2010-05-05");
			put("paramInteger", 1111);
			put("paramDecimal", 999.888);
		}
	};
	private static final Object JOB_GROUP_DESCRIPTION = "The job group name";
	private static final Object JOB_NAME_DESCRIPTION = "The job name";
	private static final Object JOB_START_DATE_DESCRIPTION = "The start date and time as dd-MM-yyyy hh:mm:ss";
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
	public void testScheduleCron() throws Exception {
		final ScheduleCronRequest requestObject = new ScheduleCronRequest();
		requestObject.setDesignFile(PARAM_TEST_RPTDESIGN);
		requestObject.setFormat("pdf");
		requestObject.setParameters(PARAM_MAP_1);
		requestObject.setOutputName("test_submit_out_1");
		final long time = System.currentTimeMillis() + 31L * 24L * 60L * 60L * 1000L;
		log.info("cron time = " + new Date(time));
		requestObject.setCronString(getCronString(time));
		requestObject.setGroup("test-group-1");
		requestObject.setName("test-1");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testScheduleCron request = " + requestString);
		final MvcResult result = this.mockMvc
				.perform(post("/schedule-cron").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("schedule-cron", requestFields(
						fieldWithPath("group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("cronString").description("The cron string as described in cron documentation"),
						fieldWithPath("misfireInstruction").optional()
								.description("One of 'ignore', 'fire-and-proceed', or 'do-nothing'"),
						fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("outputName").optional().type(JsonFieldType.STRING)
								.description(OUTPUT_NAME_DESCRIPTION),
						subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION)),
						responseFields(
								fieldWithPath("message")
										.description("Returns the exception message string in case of failure"),
								fieldWithPath("jobKey.group").description("The job group name passed in the request"),
								fieldWithPath("jobKey.name").description("The job name passed in the request"))))
				.andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("testScheduleCron response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponse = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponse.get("jobKey");
		log.info("testScheduleCron jobKey = " + jobKey);
		final String message = (String) scheduleResponse.get("message");
		log.info("testScheduleCron message = " + message);
		Assert.assertFalse("Message is not null and not blank: " + message,
				message != null && message.trim().length() > 0);
		Assert.assertNotNull("Job key is null", jobKey);
	}

	private static String getCronString(final long time) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		final List<String> args = new ArrayList<>();
		args.add(String.valueOf(cal.get(Calendar.SECOND)));
		args.add(String.valueOf(cal.get(Calendar.MINUTE)));
		args.add(String.valueOf(cal.get(Calendar.HOUR)));
		args.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
		args.add(String.valueOf(cal.get(Calendar.MONTH) + 1)); // note: javadocs are wrong for this
		args.add("?"); // day of week
		args.add(String.valueOf(cal.get(Calendar.YEAR)));
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final String arg : args) {
			sb.append(sep);
			sep = " ";
			sb.append(arg);
		}
		return sb.toString();
	}
}
