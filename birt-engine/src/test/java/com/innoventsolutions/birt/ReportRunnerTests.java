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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.service.ReportRunService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = BirtEngineApplication.class)
public class ReportRunnerTests {
	@Autowired
	ReportRunService runner;

	@Test
	public void testOneAsync() {
		final URL designURL = this.getClass().getResource("test.rptdesign");
		final String designFile = designURL.getPath();
		final ExecuteRequest reportRun = new ExecuteRequest(designFile, "Test Report", "pdf", new HashMap<>());
		try {
			//runner.execute(reportRun);
		} catch (final Throwable e) {
			e.printStackTrace();
			Assert.fail("Failed to start report: " + e);
			return;
		}
	}


}
