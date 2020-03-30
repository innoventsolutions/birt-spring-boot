package com.innoventsolutions.birt.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.innoventsolutions.birt.config.BirtProperties;
import com.innoventsolutions.birt.service.BirtEngineService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BirtEngineServiceTest {
	
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
		
		BirtProperties bp = new BirtProperties();
		File workspace = new File(appProps.getProperty("birt.runner.workspace")); 
		bp.setWorkspace(workspace);
		bp.setActuate(false);
		//bp.setBaseImageURL();
		File designDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.workspace"));
		File loggingDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.loggingDir"));
		File outputDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.outputDir"));
		File resourceDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.resourceDir"));
		File scriptLibDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.scriptLibDir"));
		File baseImageDir = new File(bp.getWorkspace(), (String)appProps.getProperty("birt.runner.baseImageURL"));
		
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
		log.info("Create Engine with: " );
		IReportEngine engine = engineService.getEngine();
		Assert.assertNotNull(engine);
	}
	
}
