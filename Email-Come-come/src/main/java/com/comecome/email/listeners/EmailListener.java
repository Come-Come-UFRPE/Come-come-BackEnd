package com.comecome.email.listeners;

import com.comecome.email.dtos.TokenDTO;
import com.comecome.email.services.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {

    public static final String fila = "send-emails";

    final EmailService emailService;

    public EmailListener(EmailService userService) {
        this.emailService = userService;
    }

    @RabbitListener(queues = fila)
    public void processar(TokenDTO message) {

        emailService.sendEmail(message);

        System.out.println("Email processado para: " + message.email());
    }

}
