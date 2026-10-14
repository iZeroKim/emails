package com.fullstack.emails.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.mail.Address;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class EmailServiceTests {

	private final CapturingMailSender mailSender = new CapturingMailSender();

	private final EmailService emailService = new EmailService(mailSender, "sender@example.com");

	@Test
	void sendsPlainTextEmail() throws Exception {
		SendEmailResult result = emailService
			.send(new SendEmailInput(List.of("a@example.com", "b@example.com"), "Hello", "Body text", false));

		assertThat(mailSender.sent).hasSize(1);
		MimeMessage message = mailSender.sent.getFirst();
		assertThat(Arrays.stream(message.getFrom()).map(Address::toString)).containsExactly("sender@example.com");
		assertThat(Arrays.stream(message.getAllRecipients()).map(Address::toString))
			.containsExactly("a@example.com", "b@example.com");
		assertThat(message.getSubject()).isEqualTo("Hello");
		assertThat(message.getContent()).isEqualTo("Body text");
		assertThat(message.getContentType()).startsWith("text/plain");
		assertThat(result.to()).containsExactly("a@example.com", "b@example.com");
		assertThat(result.sentAt()).isNotBlank();
	}

	@Test
	void sendsHtmlEmailWhenRequested() throws Exception {
		emailService.send(new SendEmailInput(List.of("a@example.com"), "Hi", "<b>Hi</b>", true));

		MimeMessage message = mailSender.sent.getFirst();
		message.saveChanges();
		assertThat(message.getContentType()).startsWith("text/html");
	}

	@Test
	void rejectsMissingRecipients() {
		assertThatThrownBy(() -> emailService.send(new SendEmailInput(List.of(), "Hi", "Body", false)))
			.isInstanceOf(InvalidEmailException.class)
			.hasMessage("At least one recipient is required");
		assertThat(mailSender.sent).isEmpty();
	}

	@Test
	void rejectsInvalidAddress() {
		assertThatThrownBy(() -> emailService.send(new SendEmailInput(List.of("not-an-email"), "Hi", "Body", false)))
			.isInstanceOf(InvalidEmailException.class)
			.hasMessage("Invalid email address: not-an-email");
		assertThat(mailSender.sent).isEmpty();
	}

	@Test
	void rejectsBlankSubject() {
		assertThatThrownBy(() -> emailService.send(new SendEmailInput(List.of("a@example.com"), " ", "Body", false)))
			.isInstanceOf(InvalidEmailException.class)
			.hasMessage("Subject must not be blank");
	}

	static class CapturingMailSender extends JavaMailSenderImpl {

		final List<MimeMessage> sent = new ArrayList<>();

		@Override
		public void send(MimeMessage... mimeMessages) {
			sent.addAll(List.of(mimeMessages));
		}

	}

}
