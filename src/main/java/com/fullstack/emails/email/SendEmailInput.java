package com.fullstack.emails.email;

import java.util.List;

public record SendEmailInput(List<String> to, String subject, String body, Boolean html) {
}
