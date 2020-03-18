package com.innoventsolutions.birt;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import sample.birt.entity.ExtendedExecuteRequest;
import sample.birt.entity.GetJobRequest;
import sample.birt.entity.ScheduleRequest;

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
	public void test() {

	}

	@Test
	public void testSchedule() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final ExtendedExecuteRequest extendedRequestObject = new ExtendedExecuteRequest();
		extendedRequestObject.setDesignFile(PARAM_TEST_RPTDESIGN);
		extendedRequestObject.setFormat("pdf");
		extendedRequestObject.setParameters(PARAM_MAP_1);
		extendedRequestObject.setOutputName("test_submit_out_1");
		final long time = System.currentTimeMillis() + 31L * 24L * 60L * 60L * 1000L;
		log.info("cron time = " + new Date(time));
		final ScheduleRequest scheduleRequestObject = new ScheduleRequest();
		scheduleRequestObject.setCronString(getCronString(time));
		scheduleRequestObject.setGroup("test-group-1");
		scheduleRequestObject.setName("test-1");
		extendedRequestObject.setSchedule(scheduleRequestObject);
		final String scheduleRequestString = mapper.writeValueAsString(extendedRequestObject);
		log.info("testScheduleCron schedule request = " + scheduleRequestString);
		final MvcResult scheduleResult = this.mockMvc
				.perform(get("/schedule").contentType(MediaType.APPLICATION_JSON).content(scheduleRequestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("schedule", requestFields(
						fieldWithPath("schedule.group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("schedule.name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("schedule.startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("schedule.cronString")
								.description("The cron string as described in cron documentation"),
						fieldWithPath("schedule.misfireInstruction").optional()
								.description("One of 'ignore', 'fire-and-proceed', or 'do-nothing'"),
						fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("outputName").optional().type(JsonFieldType.STRING)
								.description(OUTPUT_NAME_DESCRIPTION),
						subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION),
						subsectionWithPath("email").optional().type(JsonFieldType.OBJECT).description("x")),
						responseFields(
								fieldWithPath("jobKey.group").description("The job group name passed in the request"),
								fieldWithPath("jobKey.name").description("The job name passed in the request"),
								fieldWithPath("jobid").description("x"), fieldWithPath("rptDocName").description("x"),
								fieldWithPath("outFileName").description("x"),
								fieldWithPath("submitTime").description("x"),
								fieldWithPath("runBegin").description("x"), fieldWithPath("runFinish").description("x"),
								fieldWithPath("renderBegin").description("x"),
								fieldWithPath("renderFinish").description("x"),
								fieldWithPath("status").description("x"),
								subsectionWithPath("request").description("x"),
								fieldWithPath("httpStatus").description("x"),
								fieldWithPath("httpStatusMessage").description("x"),
								fieldWithPath("emailBegin").description("x"),
								fieldWithPath("emailFinish").description("x"))))
				.andReturn();
		final MockHttpServletResponse scheduleResponse = scheduleResult.getResponse();
		Assert.assertTrue(scheduleResponse.getContentType().startsWith("application/json"));
		final String jsonString = scheduleResponse.getContentAsString();
		log.info("testScheduleCron response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponseMap = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponseMap.get("jobKey");
		log.info("testScheduleCron jobKey = " + jobKey);
		final String message = (String) scheduleResponseMap.get("message");
		log.info("testScheduleCron message = " + message);
		Assert.assertFalse("Message is not null and not blank: " + message,
				message != null && message.trim().length() > 0);
		Assert.assertNotNull("Job key is null", jobKey);
	}

	@Test
	public void testGetJob() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final GetJobRequest jobRequestObject = new GetJobRequest();
		jobRequestObject.setGroup("test-group-1");
		jobRequestObject.setName("test-1");
		final String jobRequestString = mapper.writeValueAsString(jobRequestObject);
		log.info("testGetJob job request = " + jobRequestString);
		final MvcResult jobResult = this.mockMvc
				.perform(get("/job").contentType(MediaType.APPLICATION_JSON).content(jobRequestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("job",
						requestFields(
								fieldWithPath("name")
										.description(JOB_NAME_DESCRIPTION + " that was used to create the schedule"),
								fieldWithPath("group")
										.description(JOB_GROUP_DESCRIPTION + " that was used to create the schedule")),
						responseFields(
								fieldWithPath("description").description("x").type(JsonFieldType.STRING).optional(),
								fieldWithPath("jobClass").description("x").type(JsonFieldType.STRING).optional(),
								subsectionWithPath("triggers").description("The job triggers"),
								subsectionWithPath("runs")
										.description("Status info of actual runs that have occurred.  "
												+ "This is an object where each key is a report run UUID and the value is the "
												+ "same as what is returned from /status."),
								subsectionWithPath("jobKey").description("x"),
								subsectionWithPath("jobDataMap").description("x"))))
				.andReturn();
		final MockHttpServletResponse jobResponse = jobResult.getResponse();
		Assert.assertTrue(jobResponse.getContentType().startsWith("application/json"));
		final String jobResponseString = jobResponse.getContentAsString();
		log.info("testGetJob job response = " + jobResponseString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> jobResponseMap = mapper.readValue(jobResponseString, Map.class);
		log.info("responseMap = " + jobResponseMap);
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
