package com.innoventsolutions.birt.report.autoconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.service.BirtEngineService;
import com.innoventsolutions.birt.service.ReportRunService;
import com.innoventsolutions.birt.service.SubmitJobService;

@Configuration
@ConditionalOnClass(BirtEngineService.class)
@EnableConfigurationProperties(BirtConfig.class)
public class BirtAutoConfiguration {
	@Autowired
	private ReportRunService runnerService;

	@Autowired
	private SubmitJobService submitService;
}


