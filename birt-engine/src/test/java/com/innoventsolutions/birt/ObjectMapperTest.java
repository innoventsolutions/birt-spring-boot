/*******************************************************************************
 * Copyright (C) 2020 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoventsolutions.birt.entity.ExecuteRequest;

public class ObjectMapperTest {

	public ObjectMapperTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void testMapper() {
		ObjectMapper mapper = new ObjectMapper();
		String json = "{\r\n" + "	\"designFile\" : \"abc\",\r\n" + "	\"outputName\" : \"param_test\",\r\n" + "	\"format\" : \"PDF\",\r\n"
				+ "	\"parameters\" : {\"paramString\":\"String Val\", \"paramDate\":\"2010-05-05\", \"paramInt\": 1111, \"paramDecimal\":999.888}\r\n" + "\r\n" + "}";
		System.out.println(json);

		Fud f = new Fud();
		f.setDesignFile("blah");
		try {

			System.out.println(mapper.writeValueAsString(f));

			// convert JSON string to Map
			ExecuteRequest request = mapper.readValue(json, ExecuteRequest.class);

			System.out.println(request.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Fud {
		private String designFile;
		public String outputName;
		public String format;
		public Map<String, Object> parameters;

		public Fud() {
		};

		public Fud(String df, String oName, String format, Map<String, Object> p) {
			this.designFile = df;
			this.outputName = oName;
			this.format = format;
			this.parameters = p;
		}

		public Fud(String df) {
			this.designFile = df;
		}

		public String getDesignFile() {
			return designFile;
		}

		public void setDesignFile(String designFile) {
			this.designFile = designFile;
		}

		public String getOutputName() {
			return outputName;
		}

		public void setOutputName(String outputName) {
			this.outputName = outputName;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}
	}

}
