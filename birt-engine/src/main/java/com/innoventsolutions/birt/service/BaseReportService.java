package com.innoventsolutions.birt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.exception.BadRequestException;
import com.innoventsolutions.birt.exception.RunnerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseReportService {

	private static final SimpleDateFormat PARAM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	@Autowired
	protected BirtEngineService engineService;

	public BaseReportService() {
		super();
	}

	@SuppressWarnings("unused")
	protected static Object getFieldObject(final String fieldString) {
		if ("true".equalsIgnoreCase(fieldString)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(fieldString)) {
			return Boolean.FALSE;
		}
		final String trimmedFieldString = fieldString.trim();
		if (trimmedFieldString.startsWith("\"") && trimmedFieldString.endsWith("\"")) {
			return trimmedFieldString.substring(1, trimmedFieldString.length() - 1);
		}
		try {
			final int intValue = Integer.parseInt(fieldString);
			return new Integer(intValue);
		} catch (final NumberFormatException e) {
		}
		try {
			final double dblValue = Double.parseDouble(fieldString);
			return new Double(dblValue);
		} catch (final NumberFormatException e) {
		}
		try {
			final Date valDate = PARAM_DATE_FORMAT.parse(fieldString);
			return new java.sql.Date(valDate.getTime());
		} catch (final ParseException e) {
		}
		return fieldString;
	}

	protected RenderOption configureRenderOptions(final String format) {
		RenderOption options = null;
		if (format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_HTML)) {
			final HTMLRenderOption htmlOption = new HTMLRenderOption();
			htmlOption.setOutputFormat(RenderOption.OUTPUT_FORMAT_HTML);
			htmlOption.setActionHandler(new HTMLActionHandler());
			htmlOption.setImageHandler(new HTMLCompleteImageHandler());
			htmlOption.setBaseImageURL(engineService.getBaseImageURL());
			htmlOption.setImageDirectory("images");

			options = htmlOption;
		}
		if (format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_PDF)) {
			options = new PDFRenderOption();
			options.setOutputFormat(RenderOption.OUTPUT_FORMAT_PDF);
		} else {
			options = new RenderOption();
			options.setOutputFormat(format.toLowerCase());
		}
		return options;
	}

	protected IReportRunnable getRunnableReportDesign(final ExecuteRequest execRequest) throws IllegalAccessException,
			InvocationTargetException, IOException, RunnerException, BadRequestException {
		IReportRunnable design;

		try {
			final String fileName = execRequest.getDesignFile();
			File designFile;
			if (fileName.equalsIgnoreCase("TEST")) {
				designFile = new File(getClass().getClassLoader().getResource("test.rptdesign").getFile());
			} else {
				designFile = new File(execRequest.designFile);
				// Design file does not exist, look for it in the classpath
				if (!designFile.exists()) {
					final URL url = getClass().getClassLoader().getResource(fileName);
					if (url == null) {
						throw new BadRequestException(404, "Design file not found");
					}
					designFile = new File(url.getFile());
				}

				// still no design file, look for it in the workspace directory
				if (!designFile.exists() && !designFile.isAbsolute()) {
					designFile = new File(engineService.getWorkspace(), execRequest.designFile);
				}

			}

			final FileInputStream fis = new FileInputStream(designFile);
			design = engineService.getEngine().openReportDesign(fis);
		} catch (final FileNotFoundException e) {
			throw new BadRequestException(404, "Design file not found");
		} catch (final EngineException e) {
			throw new RunnerException("Failed to open report design", e);
		}
		return design;
	}

	protected void configureParameters(final ExecuteRequest execRequest, final IReportRunnable design,
			final IEngineTask task) throws IllegalAccessException, InvocationTargetException, IOException,
			RunnerException, BadRequestException {

		log.debug("configure parameters");

		final IGetParameterDefinitionTask pdTask = engineService.getEngine().createGetParameterDefinitionTask(design);
		for (final String key : execRequest.parameters.keySet()) {
			final Object paramValue = execRequest.parameters.get(key);
			final IParameterDefnBase defn = pdTask.getParameterDefn(key);
			if (defn == null) {
				throw new BadRequestException(400, "Parameter " + key + " not found in report");
			}
			final ParameterHandle handle = (ParameterHandle) defn.getHandle();
			final Object dataType = handle.getProperty("dataType");
			log.debug(" param " + key + " = " + paramValue + ", type = " + dataType + " " + defn.getTypeName());
			if (paramValue instanceof Object[]) {
				final Object[] values = (Object[]) paramValue;
				log.debug(" param " + key + " " + values.length);
				for (int i = 0; i < values.length; i++) {
					final Object value = values[i];
					log.debug("   value " + i + " " + value + " " + value.getClass().getName());
					values[i] = convertParameterValue(key + "(" + i + ")", value, dataType);
				}
				task.setParameterValue(key, values);
			} else {
				task.setParameterValue(key, convertParameterValue(key, paramValue, dataType));
			}
		}
		log.debug("validating parameters");
		task.validateParameters();

	}

	protected MediaType getMediaType(final String format) {
		if ("pdf".equalsIgnoreCase(format)) {
			return MediaType.APPLICATION_PDF;
		}
		if ("html".equalsIgnoreCase(format)) {
			return MediaType.TEXT_HTML;
		}
		if ("xls".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-excel");
		}
		if ("xlsx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
		if ("doc".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/ms-word");
		}
		if ("docx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		if ("ppt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-powerpoint");
		}
		if ("pptx".equalsIgnoreCase(format)) {
			return MediaType
					.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
		}
		if (".odp".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.presentation");
		}
		if (".ods".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet");
		}
		if (".odt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.text");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	protected Object convertParameterValue(final String name, final Object paramValue, final Object dataType)
			throws BadRequestException {
		if (paramValue instanceof String) {
			final String stringValue = (String) paramValue;
			if ("integer".equals(dataType)) {
				try {
					return Integer.valueOf(stringValue);
				} catch (final NumberFormatException e) {
					log.error("Parameter " + name + " isn't a valid integer");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid integer");
				}
			}
			if ("boolean".equals(dataType)) {
				return Boolean.valueOf(stringValue);
			}
			if ("decimal".equals(dataType)) {
				try {
					return Double.valueOf(stringValue);
				} catch (final NumberFormatException e) {
					log.error("Parameter " + name + " isn't a valid decimal");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid decimal");
				}
			}
			if ("float".equals(dataType)) {
				try {
					return Double.valueOf(stringValue);
				} catch (final NumberFormatException e) {
					log.error("Parameter " + name + " isn't a valid float");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid float");
				}
			}
			if ("date".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					return new java.sql.Date(df.parse(stringValue).getTime());
				} catch (final ParseException e) {
					log.error("Parameter " + name + " isn't a valid date");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid date");
				}
			}
			if ("dateTime".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
				try {
					return new java.sql.Date(df.parse(stringValue).getTime());
				} catch (final ParseException e) {
					log.error("Parameter " + name + " isn't a valid dateTime");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid dateTime");
				}
			}
			if ("time".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("HH:mm:dd");
				try {
					return new java.sql.Time(df.parse(stringValue).getTime());
				} catch (final ParseException e) {
					log.error("Parameter " + name + " isn't a valid time");
					throw new BadRequestException(406, "Parameter " + name + " isn't a valid time");
				}
			}
		}
		return paramValue;
	}

	protected static List<File> getPropFiles(final File baseDir) {
		final ArrayList<File> files = new ArrayList<File>();
		if (!baseDir.isDirectory()) {
			files.add(baseDir);
		} else {
			final File[] dirFile = baseDir.listFiles(new PropFilter());
			for (int i = 0; i < dirFile.length; i++) {
				files.add(dirFile[i]);
			}
		}
		return files;
	}

	protected final static class PropFilter implements FilenameFilter {
		private final String extension = ".properties";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}

	public Map<String, Object> fixParameterTypes(final Map<String, Object> parameters) throws BadRequestException {
		if (parameters == null) {
			return null;
		}
		final Map<String, Object> fixedParameters = new HashMap<>();
		for (final String paramName : parameters.keySet()) {
			Object paramValue = parameters.get(paramName);
			if (paramValue instanceof Object[]) {
				final Object[] valueArray = (Object[]) paramValue;
				for (int i = 0; i < valueArray.length; i++) {
					valueArray[i] = fixParameterType(paramName, valueArray[i]);
				}
			}
			paramValue = fixParameterType(paramName, paramValue);
			fixedParameters.put(paramName, paramValue);
		}
		return fixedParameters;
	}

	private Object fixParameterType(final Object name, final Object value) throws BadRequestException {
		if (!(value instanceof Map)) {
			return value;
		}
		final Map<?, ?> map = (Map<?, ?>) value;
		final Object type = map.get("type");
		if (type == null) {
			log.error("parameter value type is missing");
			throw new BadRequestException(406, "Parameter " + name + " is an object but the type field is missing");
		}
		final Object subValue = map.get("value");
		if (!(subValue instanceof String)) {
			log.error("parameter sub-value is not a string");
			throw new BadRequestException(406,
					"Parameter " + name + " is an object but the value field is missing or isn't a string");
		}
		if ("date".equals(type)) {
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Date(date.getTime());
			} catch (final ParseException e) {
				log.error("parameter date sub-value is malformed");
				throw new BadRequestException(406,
						"Parameter " + name + " is an object and the type is date but the value isn't a valid date");
			}
		}
		if ("datetime".equals(type)) {
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Date(date.getTime());
			} catch (final ParseException e) {
				log.error("parameter date sub-value is malformed");
				throw new BadRequestException(406, "Parameter " + name
						+ " is an object and the type is datetime but the value isn't a valid datetime");
			}
		}
		if ("time".equals(type)) {
			final DateFormat df = new SimpleDateFormat("HH:mm:ss");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Time(date.getTime());
			} catch (final ParseException e) {
				log.error("parameter date sub-value is malformed");
				throw new BadRequestException(406,
						"Parameter " + name + " is an object and the type is time but the value isn't a valid time");
			}
		}
		log.error("unrecognized parameter value type: " + type);
		throw new BadRequestException(406,
				"Parameter " + name + " is an object and the type field is present but is not recognized");
	}

}