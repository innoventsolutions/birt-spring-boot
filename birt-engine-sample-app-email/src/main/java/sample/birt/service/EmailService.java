package sample.birt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;

import lombok.extern.slf4j.Slf4j;
import sample.birt.config.EmailConfig;
import sample.birt.entity.EmailRequest;
import sample.birt.entity.SubmitAndEmailResponse;

@Slf4j
@Service
public class EmailService {
	@Autowired
	private EmailConfig config;

	@Autowired
	private BirtConfig birtConfig;

	public SubmitAndEmailResponse send(final EmailRequest emailRequest, final SubmitAndEmailResponse submitResponse) {
		submitResponse.setEmailBegin(new Date());
		final File smtpPropertiesFile = config.getSmtpPropertiesFile();
		if (smtpPropertiesFile == null) {
			log.info("Email not sent because SMTP properties file not specified");
			return submitResponse;
		}
		if (!smtpPropertiesFile.exists()) {
			log.info("Email not sent because SMTP properties file " + smtpPropertiesFile.getAbsolutePath()
					+ " does not exist");
			return submitResponse;
		}
		final String mailFrom = config.getFrom();
		if (mailFrom == null) {
			log.info("Email not sent because email-to not specified");
			return submitResponse;
		}
		if (emailRequest == null) {
			log.info("Email not sent because email info is missing from the request");
			return submitResponse;
		}
		final boolean success = StatusEnum.COMPLETE.equals(submitResponse.getStatus());
		final boolean sendOnSuccess = emailRequest.getSuccess() != null ? emailRequest.getSuccess().booleanValue()
				: config.isSuccess();
		if (!sendOnSuccess) {
			if (success) {
				log.info("Email not sent because success emails not wanted");
				return submitResponse;
			}
		}
		final boolean sendOnFailure = emailRequest.getFailure() != null ? emailRequest.getFailure().booleanValue()
				: config.isFailure();
		if (!sendOnFailure) {
			if (!success) {
				log.info("Email not sent because failure emails not wanted");
				return submitResponse;
			}
		}
		log.info("Mailer is sending email");
		final Properties emailProperties = new Properties();
		try {
			emailProperties.load(new FileInputStream(smtpPropertiesFile));
		} catch (final IOException e1) {
			log.error("Unable to load smtp properties from file " + smtpPropertiesFile, e1);
			submitResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			submitResponse.setHttpStatusMessage("Unable to load SMTP properties file");
			return submitResponse;
		}
		log.info("emailPropertiesFile = " + smtpPropertiesFile);
		final Session session;
		final String username = config.getUsername();
		final String password = config.getPassword();
		if (username != null && password != null) {
			log.info("Creating email session with authentication");
			final PasswordAuthentication pa = new PasswordAuthentication(username, password);
			session = Session.getDefaultInstance(emailProperties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return pa;
				}
			});
		} else {
			log.info("Creating email session without authentication");
			session = Session.getDefaultInstance(emailProperties);
		}
		session.setDebug(true);
		final String mailTo = join(config.getTo(), emailRequest.getTo());
		final String[] recipients = mailTo.split(", *");
		final Map<String, Exception> exceptions = new HashMap<>();
		for (final String recipient : recipients) {
			final MimeMessage mimeMessage = new MimeMessage(session);
			try {
				mimeMessage.setFrom(new InternetAddress(mailFrom));
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				final String mailCc = join(config.getCc(), emailRequest.getCc());
				if (!mailCc.trim().isEmpty()) {
					final String[] ccArray = mailCc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
						}
					}
				}
				final String mailBcc = join(config.getBcc(), emailRequest.getBcc());
				if (!mailBcc.trim().isEmpty()) {
					final String[] ccArray = mailBcc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(cc));
						}
					}
				}
				final String mailSuccessSubject = supercede(config.getSuccessSubject(),
						emailRequest.getSuccessSubject());
				final String mailFailureSubject = supercede(config.getFailureSubject(),
						emailRequest.getFailureSubject());
				final String subject = success
						? mailSuccessSubject.trim().isEmpty() ? "Success" : submitResponse.replace(mailSuccessSubject)
						: mailFailureSubject.trim().isEmpty() ? "Failure" : submitResponse.replace(mailFailureSubject);
				mimeMessage.setSubject(subject);
				final String mailSuccessBody = supercede(config.getSuccessBody(), emailRequest.getSuccessBody());
				final String mailFailureBody = supercede(config.getFailureBody(), emailRequest.getFailureBody());
				final String body = success ? submitResponse.replace(mailSuccessBody)
						: mailFailureBody.trim().isEmpty() ? null : submitResponse.replace(mailFailureBody);
				final Multipart mp = new MimeMultipart();
				if (body != null) {
					final MimeBodyPart mbp = new MimeBodyPart();
					final boolean html = emailRequest.getHtml() != null ? emailRequest.getHtml().booleanValue()
							: config.isHtml();
					mbp.setContent(body, html ? "text/html;charset=utf-8" : "text/plain");
					mp.addBodyPart(mbp);
				}
				final boolean attach = emailRequest.getAttachReport() != null
						? emailRequest.getAttachReport().booleanValue()
						: config.isAttachReport();
				final File outputFile = new File(birtConfig.getOutputDir(), submitResponse.getRptDocName());
				if (attach && success && outputFile.exists()) {
					final MimeBodyPart mbp = new MimeBodyPart();
					final DataSource dataSource = new FileDataSource(outputFile);
					mbp.setDataHandler(new DataHandler(dataSource));
					mbp.setFileName(submitResponse.getRptDocName());
					mp.addBodyPart(mbp);
				}
				mimeMessage.setContent(mp);
				mimeMessage.setSentDate(new Date());
				Transport.send(mimeMessage);
			} catch (final MessagingException e) {
				log.error("Failed to send email to " + recipient, e);
				exceptions.put(recipient, e);
			}
		}

		if (exceptions.isEmpty()) {
			submitResponse.setEmailFinish(new Date());
		} else {
			submitResponse.setStatus(StatusEnum.EXCEPTION);
		}
		return submitResponse;
	}

	private static String join(String s1, String s2) {
		if (s1 == null) {
			s1 = "";
		}
		if (s2 == null) {
			s2 = "";
		}
		if (!s2.trim().isEmpty()) {
			if (!s1.trim().isEmpty()) {
				s1 += ", ";
			}
			s1 += s2;
		}
		return s1;
	}

	private static String supercede(String s1, String s2) {
		if (s1 == null) {
			s1 = "";
		}
		if (s2 == null) {
			s2 = "";
		}
		if (!s2.trim().isEmpty()) {
			return s2;
		}
		return s1;
	}
}
