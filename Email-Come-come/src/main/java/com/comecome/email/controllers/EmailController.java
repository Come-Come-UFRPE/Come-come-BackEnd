package com.comecome.email.controllers;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final JavaMailSender mailSender;

    public EmailController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RequestMapping("/send-email")
    public String sendEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("contatesteapify1@gmail.com");
            message.setTo("contatesteapify1@gmail.com");
            message.setSubject("Email Conta Corrente");
            message.setText("Email Conta Corrente");

            mailSender.send(message);
            return "Teste";
        } catch (Exception e) {
            e.getMessage();
            return "Error";
        }
    }
}
