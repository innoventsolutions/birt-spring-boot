package com.innoventsolutions.birt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.service.ReportRunService;

@SpringBootApplication
class BirtEngineApplicationTest {

	@Autowired
	private ReportRunService runnerService;

	public static void main(final String[] args) {
		SpringApplication.run(BirtEngineApplicationTest.class, args);
	}


	public void run(final String... args) throws Exception {
		final String designFile = "/disk1/home/innovent/projects/birt-spring-boot-starter-mvn/test.rptdesign";
		final ExecuteRequest reportRun = new ExecuteRequest(designFile, "Test Report", "pdf");
		//runnerService.execute(reportRun);
	}
}
