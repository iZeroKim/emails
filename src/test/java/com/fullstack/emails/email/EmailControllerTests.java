package com.fullstack.emails.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(EmailController.class)
@Import(EmailExceptionResolver.class)
class EmailControllerTests {

	private static final String SEND = """
			mutation {
			  sendEmail(input: {to: ["a@example.com"], subject: "Hello", body: "Hi"}) {
			    to
			    subject
			    sentAt
			  }
			}
			""";

	@Autowired
	private GraphQlTester graphQlTester;

	@MockitoBean
	private EmailService emailService;

	@Test
	void ping() {
		graphQlTester.document("{ ping }").execute().path("ping").entity(String.class).isEqualTo("pong");
	}

	@Test
	void sendEmailReturnsResult() {
		given(emailService.send(new SendEmailInput(List.of("a@example.com"), "Hello", "Hi", false)))
			.willReturn(new SendEmailResult(List.of("a@example.com"), "Hello", "2026-10-14T12:00:00Z"));

		graphQlTester.document(SEND)
			.execute()
			.path("sendEmail.to").entityList(String.class).containsExactly("a@example.com")
			.path("sendEmail.subject").entity(String.class).isEqualTo("Hello")
			.path("sendEmail.sentAt").entity(String.class).isEqualTo("2026-10-14T12:00:00Z");
	}

	@Test
	void invalidInputIsBadRequest() {
		given(emailService.send(any())).willThrow(new InvalidEmailException("Invalid email address: x"));

		graphQlTester.document(SEND)
			.execute()
			.errors()
			.expect(error -> error.getErrorType() == ErrorType.BAD_REQUEST
					&& error.getMessage().equals("Invalid email address: x"))
			.verify();
	}

	@Test
	void smtpFailureIsInternalErrorWithoutDetails() {
		given(emailService.send(any())).willThrow(new MailSendException("Connection refused: smtp.internal:25"));

		graphQlTester.document(SEND)
			.execute()
			.errors()
			.expect(error -> error.getErrorType() == ErrorType.INTERNAL_ERROR
					&& error.getMessage().equals("Failed to send email"))
			.verify();
	}

}
