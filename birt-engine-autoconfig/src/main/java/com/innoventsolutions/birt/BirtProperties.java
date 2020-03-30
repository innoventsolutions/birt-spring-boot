package com.innoventsolutions.birt;

import java.io.File;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innoventsolutions.birt.config.BirtConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConfigurationProperties(prefix = "birt.runner")
public class BirtProperties extends BirtConfig implements Serializable {

	private static final long serialVersionUID = -6821187546631689762L;
	
	@PostConstruct
	public void setDefaults() {
		
		log.info("Set Defaults");
		if (this.getWorkspace() == null) 
			this.setWorkspace(new File ("../birt-engine-workspace"));
		
		if (this.getDesignDir() == null) 
			this.setDesignDir(new File(this.getWorkspace(), "rptdesign"));
		
		if (this.getLoggingDir() == null)
			this.setLoggingDir(new File(this.getWorkspace(), "log"));
		
		if (this.getOutputDir() == null)
			this.setOutputDir(new File(this.getWorkspace(), "output"));
		
		if (this.getReportFormat() == null)
			this.setReportFormat("PDF");
		
		if (this.getResourceDir() == null)
			this.setResourceDir(new File(this.getWorkspace(), "resources"));
		
		if (this.getScriptLibDir() == null)
			this.setScriptLibDir(new File(this.getWorkspace(), "lib"));

		if (this.getBaseImageURL() == null) {
			File imageBase = new File(this.getWorkspace(), "images");
			this.setBaseImageURL(imageBase.getAbsolutePath());
		}
		
		// Add test and warning if no report design folder is present 
		File designDir = this.getDesignDir();
		if (!designDir.exists() || !designDir.isDirectory() ) {
			StringBuffer sb = new StringBuffer();
			sb.append("Design Dir does not exist or is not a folder. ");
			sb.append("\nWorkspace Location: ").append(this.getWorkspace().getAbsolutePath());
			sb.append((this.getWorkspace().exists()) ? "(exists)" : " (does not exist)");
			String ddd = ( this.getDesignDir() == null) ? "not set" : this.getDesignDir().getAbsolutePath(); 
			sb.append("\nDesign Dir: ").append(ddd);
			sb.append((this.getDesignDir().exists()) ? "(exists)" : " (does not exist)");
			sb.append("\nCheck application.properties file");
			log.warn(sb.toString());
		}
		if (designDir.exists() && designDir.isDirectory() && designDir.listFiles().length == 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("Design Dir exists, but does not contain any file or sub-folders ");
			sb.append("\nWorkspace Location: ").append(this.getWorkspace().getAbsolutePath());
			sb.append((this.getWorkspace().exists()) ? "(exists)" : " (does not exist)");
			String ddd = ( this.getDesignDir() == null) ? "not set" : this.getDesignDir().getAbsolutePath(); 
			sb.append("\nDesign Dir: ").append(ddd);
			sb.append((this.getDesignDir().exists()) ? "(exists)" : " (does not exist)");
			sb.append("\nCheck application.properties file");
			log.warn(sb.toString());
		}

		
			
	}

}
