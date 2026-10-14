package com.fullstack.emails.email;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

/**
 * Maps email failures to GraphQL errors: bad input becomes BAD_REQUEST with the
 * validation message, SMTP failures become INTERNAL_ERROR without server details.
 */
@Component
public class EmailExceptionResolver extends DataFetcherExceptionResolverAdapter {

	private static final Logger log = LoggerFactory.getLogger(EmailExceptionResolver.class);

	@Override
	protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
		if (ex instanceof InvalidEmailException) {
			return GraphqlErrorBuilder.newError(env).errorType(ErrorType.BAD_REQUEST).message(ex.getMessage()).build();
		}
		if (ex instanceof MailException) {
			log.error("Failed to send email", ex);
			return GraphqlErrorBuilder.newError(env).errorType(ErrorType.INTERNAL_ERROR).message("Failed to send email").build();
		}
		return null;
	}

}
