package com.innoventsolutions.brrs;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innoventsolutions.brrs.report.ReportRun;
import com.innoventsolutions.brrs.report.service.RunnerService;

// if this annotation is not commented out then run will execute
@SpringBootApplication
public class BirtEngineTestApplication implements CommandLineRunner {

	@Autowired
	private RunnerService runnerService;

	public static void main(final String[] args) {
		SpringApplication.run(BirtEngineTestApplication.class, args);
	}

	@Override
	public void run(final String... args) throws Exception {
		final String designFile = "/disk1/home/innovent/projects/birt-spring-boot-starter-mvn/test.rptdesign";
		final ReportRun reportRun = new ReportRun(designFile, "Test Report", "pdf", "test.pdf", true, new HashMap<>());
		runnerService.runReport(reportRun);
	}
}
