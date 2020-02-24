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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.innoventsolutions.birt.BirtEngineApplication;
import com.innoventsolutions.birt.report.ReportRun;
import com.innoventsolutions.birt.report.ReportRunStatus;
import com.innoventsolutions.birt.report.service.RunnerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BirtEngineApplication.class)
public class ReportRunnerTests {
	@Autowired
	RunnerService runner;

	@Test
	public void testOneAsync() {
		final URL designURL = this.getClass().getResource("test.rptdesign");
		final String designFile = designURL.getPath();
		final ReportRun reportRun = new ReportRun(designFile, "Test Report", "pdf", "test.pdf", true, new HashMap<>());
		try {
			runner.startReport(reportRun);
		} catch (final Throwable e) {
			e.printStackTrace();
			Assert.fail("Failed to start report: " + e);
			return;
		}
		runner.waitForAllReportsToFinish();
	}

	@Test
	public void testMultiAsync() {
		final URL designURL = this.getClass().getResource("test.rptdesign");
		final String designFile = designURL.getPath();
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("keyFilter", "a.*");
		final ReportRun[] reportRuns = new ReportRun[] {
				new ReportRun(designFile, "Test Report 1", "pdf", "test1.pdf", true, new HashMap<>()),
				new ReportRun(designFile, "Test Report 2", "pdf", "test2.pdf", false, new HashMap<>()),
				new ReportRun(designFile, "Test Report 3", "pdf", "test3.pdf", true, parameters),
				new ReportRun(designFile, "Test Report 4", "pdf", "test4.pdf", false, parameters) };
		final UUID[] uuids = new UUID[reportRuns.length];
		for (int i = 0; i < reportRuns.length; i++) {
			final ReportRun reportRun = reportRuns[i];
			try {
				uuids[i] = runner.startReport(reportRun);
			} catch (final Throwable e) {
				e.printStackTrace();
				Assert.fail("Failed to start report: " + e);
				return;
			}
		}
		runner.waitForAllReportsToFinish();
		for (final UUID uuid : uuids) {
			final ReportRunStatus status = runner.reports.get(uuid);
			Assert.assertNotNull("Status for " + uuid + " is null", status);
			for (final Exception e : status.getErrors()) {
				log.info("Exception for " + uuid + " = " + e);
			}
			Assert.assertTrue(status.getErrors().isEmpty());
		}
	}
}
