package com.fullstack.emails.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	private final String from;

	public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	public SendEmailResult send(SendEmailInput input) {
		validate(input);
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			helper.setFrom(from);
			helper.setTo(input.to().toArray(String[]::new));
			helper.setSubject(input.subject());
			helper.setText(input.body(), Boolean.TRUE.equals(input.html()));
		}
		catch (MessagingException ex) {
			throw new MailPreparationException("Could not build email", ex);
		}
		mailSender.send(message);
		return new SendEmailResult(input.to(), input.subject(), Instant.now().toString());
	}

	private void validate(SendEmailInput input) {
		if (input.to() == null || input.to().isEmpty()) {
			throw new InvalidEmailException("At least one recipient is required");
		}
		for (String address : input.to()) {
			try {
				new InternetAddress(address, true).validate();
			}
			catch (AddressException ex) {
				throw new InvalidEmailException("Invalid email address: " + address);
			}
		}
		if (input.subject() == null || input.subject().isBlank()) {
			throw new InvalidEmailException("Subject must not be blank");
		}
	}

}
