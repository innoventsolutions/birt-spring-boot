package sample.birt.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.config.BirtConfig;
import com.innoventsolutions.birt.entity.SubmitResponse.StatusEnum;

import lombok.extern.slf4j.Slf4j;
import sample.birt.config.EmailConfig;
import sample.birt.entity.EmailRequest;
import sample.birt.entity.ExtendedSubmitResponse;

@Slf4j
@Service
public class EmailService {
	@Autowired
	private EmailConfig config;

	@Autowired
	private BirtConfig birtConfig;

	@Autowired
	private JavaMailSender javaMailSender;

	public ExtendedSubmitResponse send(final EmailRequest emailRequest, final ExtendedSubmitResponse submitResponse) {
		submitResponse.setEmailBegin(new Date());
		final String mailFrom = config.getFrom();
		if (mailFrom == null) {
			log.info("Email not sent because email-to not specified");
			return submitResponse;
		}
		if (emailRequest != null && emailRequest.getEnable().booleanValue() == false) {
			log.info("Email not sent because it was disabled in the request");
			return submitResponse;
		}
		final boolean success = StatusEnum.COMPLETE.equals(submitResponse.getStatus());
		final boolean sendOnSuccess = emailRequest != null && emailRequest.getSuccess() != null
				? emailRequest.getSuccess().booleanValue()
				: config.isSuccess();
		if (!sendOnSuccess) {
			if (success) {
				log.info("Email not sent because success emails not wanted");
				return submitResponse;
			}
		}
		final boolean sendOnFailure = emailRequest != null && emailRequest.getFailure() != null
				? emailRequest.getFailure().booleanValue()
				: config.isFailure();
		if (!sendOnFailure) {
			if (!success) {
				log.info("Email not sent because failure emails not wanted");
				return submitResponse;
			}
		}
		log.info("Mailer is sending email");
		final String mailTo = join(config.getTo(), emailRequest != null ? emailRequest.getTo() : "");
		final String[] recipients = mailTo.split(", *");
		final Map<String, Exception> exceptions = new HashMap<>();
		for (final String recipient : recipients) {
			final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			try {
				mimeMessage.setFrom(new InternetAddress(mailFrom));
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				final String mailCc = join(config.getCc(), emailRequest != null ? emailRequest.getCc() : "");
				if (!mailCc.trim().isEmpty()) {
					final String[] ccArray = mailCc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
						}
					}
				}
				final String mailBcc = join(config.getBcc(), emailRequest != null ? emailRequest.getBcc() : "");
				if (!mailBcc.trim().isEmpty()) {
					final String[] ccArray = mailBcc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(cc));
						}
					}
				}
				final String mailSuccessSubject = supercede(config.getSuccessSubject(),
						emailRequest != null ? emailRequest.getSuccessSubject() : "");
				final String mailFailureSubject = supercede(config.getFailureSubject(),
						emailRequest != null ? emailRequest.getFailureSubject() : "");
				final String subject = success
						? mailSuccessSubject.trim().isEmpty() ? "Success" : submitResponse.replace(mailSuccessSubject)
						: mailFailureSubject.trim().isEmpty() ? "Failure" : submitResponse.replace(mailFailureSubject);
				mimeMessage.setSubject(subject);
				final String mailSuccessBody = supercede(config.getSuccessBody(),
						emailRequest != null ? emailRequest.getSuccessBody() : "");
				final String mailFailureBody = supercede(config.getFailureBody(),
						emailRequest != null ? emailRequest.getFailureBody() : "");
				final String body = success ? submitResponse.replace(mailSuccessBody)
						: mailFailureBody.trim().isEmpty() ? null : submitResponse.replace(mailFailureBody);
				final Multipart mp = new MimeMultipart();
				if (body != null) {
					final MimeBodyPart mbp = new MimeBodyPart();
					final boolean html = emailRequest != null && emailRequest.getHtml() != null
							? emailRequest.getHtml().booleanValue()
							: config.isHtml();
					mbp.setContent(body, html ? "text/html;charset=utf-8" : "text/plain");
					mp.addBodyPart(mbp);
				}
				final boolean attach = emailRequest != null && emailRequest.getAttachReport() != null
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
				javaMailSender.send(mimeMessage);
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
