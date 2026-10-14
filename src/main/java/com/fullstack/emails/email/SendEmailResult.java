package com.fullstack.emails.email;

import java.util.List;

public record SendEmailResult(List<String> to, String subject, String sentAt) {
}
