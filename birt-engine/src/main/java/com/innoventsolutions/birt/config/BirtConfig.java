package com.innoventsolutions.birt.config;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
@Component
@ConfigurationProperties(prefix = "birt.runner")
public class BirtConfig {
	private File workspace;
	private File designDir;
	private File loggingDir;
	private File outputDir;
	private File resourceDir;
	private File scriptLibDir;
	private String baseImageURL;
	private File birtRuntimeHome;
	private String reportFormat;
	private int threadCount;
	private boolean isActuate;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("BIRTConfig: Workspace Loc = ").append(workspace.getAbsolutePath());
		sb.append(" BirtRuntimeHome = ").append(birtRuntimeHome);
		return sb.toString();
	}
}
