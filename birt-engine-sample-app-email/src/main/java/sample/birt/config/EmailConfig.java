package sample.birt.config;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email.")
public class EmailConfig {
	private File smtpPropertiesFile = new File("../smtp.properties");
	private String mailUsername;
	private String mailPassword;
	private String mailFrom;
	private String mailTo;
	private String mailCc;
	private String mailBcc;
	private boolean mailSuccess;
	private boolean mailFailure;
	private String mailSuccessSubject;
	private String mailFailureSubject;
	private String mailSuccessBody;
	private String mailFailureBody;
	private boolean mailHtml;
	private boolean mailAttachReport;
}
