package com.innoventsolutions.birt.config;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "birt.runner")
public class BirtConfig {
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
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
