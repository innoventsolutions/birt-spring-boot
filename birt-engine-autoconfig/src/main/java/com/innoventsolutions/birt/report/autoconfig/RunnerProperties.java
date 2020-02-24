package com.innoventsolutions.birt.report.autoconfig;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "birt.runner")
@Getter @Setter
public class RunnerProperties {
	private File outputDirectory;
	private File workspace;
	private File birtRuntimeHome;
	private File resourcePath;
	private File scriptLib;
	private File loggingPropertiesFile;
	private File loggingDir;
	private String reportFormat;
	private String baseImageURL;
	private int threadCount;
	private boolean isActuate;
	private Pattern unsecuredDesignFilePattern;
	private Pattern unsecuredOperationPattern;
}
