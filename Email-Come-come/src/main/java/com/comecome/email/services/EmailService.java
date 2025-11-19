package com.comecome.email.services;

import com.comecome.email.controllers.EmailController;
import com.comecome.email.dtos.TokenDTO;
import jakarta.mail.internet.MimeMessage;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(TokenDTO dto){
        try {
            // Cria uma estrutura de mensagem carregada para o HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Configurando remetente, destinatário e tópico do email
            helper.setFrom("contatesteapify1@gmail.com");
            helper.setTo(dto.email());
            helper.setSubject("Solicitação de Redefinição de Senha | Come-come");

            // Passa o HTML lá em resources para String e formata com o token que chegou
            try(var inputStream = Objects.requireNonNull(EmailController.class.getResourceAsStream("/templates/email-content.html"))) {

                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String templateFinal = template.replace("{{TOKEN}}", dto.token());

                helper.setText(templateFinal, true);
            }

            // Envia o email
            mailSender.send(message);
            System.out.println("Email enviado com sucesso para: " + dto.email());

        } catch (Exception e) {
            //Caso dê erro
            System.err.println("Erro ao processar mensagem da fila: " + e.getMessage());
            e.printStackTrace();

        }
    }
}

