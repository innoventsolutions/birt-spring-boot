package sample.birt.config;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "birt.runner.mail")
public class EmailConfig {
	private File smtpPropertiesFile;
	private String username;
	private String password;
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private boolean success;
	private boolean failure;
	private String successSubject;
	private String failureSubject;
	private String successBody;
	private String failureBody;
	private boolean html;
	private boolean attachReport;
}
