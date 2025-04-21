package com.berkan.user_service.service;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String text);
}
