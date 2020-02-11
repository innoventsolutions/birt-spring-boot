package com.innoventsolutions.brrs;

import java.net.URL;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innoventsolutions.brrs.report.ReportRun;
import com.innoventsolutions.brrs.report.service.RunnerService;

@SpringBootApplication
public class BirtEngineTestApplication implements CommandLineRunner {

	@Autowired
	private RunnerService runnerService;

	public static void main(String[] args) {
		SpringApplication.run(BirtEngineTestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		URL rptdesignUrl = this.getClass().getResource("test.rptdesign");
		String designFile = rptdesignUrl.getPath();
		ReportRun reportRun = new ReportRun(designFile, "Test Report", "pdf", "test.pdf", true, new HashMap<>());
		runnerService.runReport(reportRun);
	}
}
