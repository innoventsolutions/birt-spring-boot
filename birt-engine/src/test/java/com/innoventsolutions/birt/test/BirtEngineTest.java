package com.innoventsolutions.birt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.birt.report.engine.api.EmitterInfo;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;
import com.innoventsolutions.birt.service.BirtEngineService;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.service.SubmitJobService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BirtEngineTest {
	// the HTML header
	static final String HTML_INTRO = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"; 
	// random birt script in the test report
	static final String RPT_DOC_STR = "dsKeys = new Packages.java.util.ArrayList();"; 
	BirtEngineService engineService;

	@Before
	public void setUp() {
		InputStream inputStream = null;
		Properties appProps = new Properties();
		try {
			inputStream = this.getClass().getResourceAsStream("/app.properties");
			appProps.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BirtConfig bp = new BirtConfig();
		File workspace = new File(appProps.getProperty("birt.runner.workspace"));
		bp.setWorkspace(workspace);
		bp.setActuate(false);
		// bp.setBaseImageURL();
		File designDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.designDir"));
		File loggingDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.loggingDir"));
		File outputDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.outputDir"));
		File resourceDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.resourceDir"));
		File scriptLibDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.scriptLibDir"));
		File baseImageDir = new File(bp.getWorkspace(), (String) appProps.getProperty("birt.runner.baseImageURL"));

		bp.setBirtRuntimeHome(null);
		bp.setDesignDir(designDir);
		bp.setLoggingDir(loggingDir);
		bp.setOutputDir(outputDir);
		bp.setReportFormat(appProps.getProperty("birt.runner.format"));
		bp.setResourceDir(resourceDir);
		bp.setScriptLibDir(scriptLibDir);
		bp.setBaseImageURL(baseImageDir.getAbsolutePath());

		this.engineService = new BirtEngineService(bp);
	}

	@Test
	public void testEngine() {
		log.info("Create Engine with: ");
		IReportEngine engine = engineService.getEngine();
		Assert.assertNotNull(engine);
		@SuppressWarnings("unused")
		Object rootScope = engine.getRootScope();
		@SuppressWarnings("unused")
		EmitterInfo[] emitterInfo = engine.getEmitterInfo();
		String[] formats = engine.getSupportedFormats();
		Assert.assertEquals(13, formats.length);
		List<String> formatList = (List<String>) Arrays.asList(formats);
		Assert.assertTrue(formatList.contains("pdf"));
		Assert.assertTrue(formatList.contains("html"));
		Assert.assertTrue(formatList.contains("xlsx"));
		Assert.assertTrue(formatList.contains("pptx"));
		Assert.assertTrue(formatList.contains("docx"));
		Assert.assertTrue(formatList.contains("odp"));
		Assert.assertTrue(formatList.contains("xls"));
		/*
		 * for (int i = 0; i < formats.length; i++) { log.info(formats[i]); }
		 */
	}

	@Test
	public void testRunReport() throws IOException {
		ReportRunService rrs = new ReportRunService(engineService);
		Assert.assertNotNull(rrs);

		MockHttpServletResponse httpResp = new MockHttpServletResponse();

		Assert.assertNotNull(httpResp);
		ExecuteRequest execReq = new ExecuteRequest();
		execReq.setDesignFile("test.rptdesign");
		execReq.setFormat("HTML");
		execReq.setWrapError(false);

		rrs.execute(execReq, httpResp.getOutputStream());

		Assert.assertTrue(httpResp.getContentAsString().startsWith(HTML_INTRO));

	}

	@Test
	public void testSubmitReport() {

		File outputDir = engineService.getOutputDir();
		String outputFileName = "TEST_123456789";
		FilenameFilter filter = (dir, name) -> name.startsWith(outputFileName);
		File[] curFiles = outputDir.listFiles(filter);
		if(curFiles != null) {
			for (int i = 0; i < curFiles.length; i++) {
				curFiles[i].delete();
			}
		}

		ForkJoinPool ffjp = new ForkJoinPool(5);
		SubmitJobService sjs = new SubmitJobService(engineService, ffjp);
		ExecuteRequest execReq = new ExecuteRequest();
		execReq.setDesignFile("test.rptdesign");
		execReq.setOutputName(outputFileName);
		execReq.setFormat("HTML");
		execReq.setWrapError(false);

		SubmitResponse submitResponse = new SubmitResponse(execReq);
		sjs.executeRun(submitResponse);
		sjs.executeRender(submitResponse);

		filter = (dir, name) -> name.startsWith(outputFileName);
		curFiles = outputDir.listFiles(filter);
		Assert.assertTrue(2 == curFiles.length);
		for (int i = 0; i < curFiles.length; i++) {
			if (curFiles[i].getName().endsWith(".rptdocument")) {
				Assert.assertTrue(fileSearch(curFiles[i], RPT_DOC_STR) > 0);
			} else {
				Assert.assertTrue(fileSearch(curFiles[i], HTML_INTRO) == 0);
			}
		}

	}

	private int fileSearch(File f, final String cStr) {
		try {
			StringBuffer sb = new StringBuffer();
			FileInputStream fis = new FileInputStream(f);
			int content;
			while ((content = fis.read()) != -1) {
				sb.append((char) content);
			}
			fis.close();
			return sb.indexOf(cStr);

		} catch (IOException e) {
			log.error("Failure to find string: " + e.getMessage());
		}
		return -1;
	}
}
