package com.fullstack.emails.email;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class EmailController {

	private final EmailService emailService;

	public EmailController(EmailService emailService) {
		this.emailService = emailService;
	}

	@QueryMapping
	public String ping() {
		return "pong";
	}

	@MutationMapping
	public SendEmailResult sendEmail(@Argument SendEmailInput input) {
		return emailService.send(input);
	}

}
