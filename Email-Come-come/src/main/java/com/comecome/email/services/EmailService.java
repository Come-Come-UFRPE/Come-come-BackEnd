package com.comecome.email.services;

import com.comecome.email.controllers.EmailController;
import com.comecome.email.dtos.TokenDTO;
import jakarta.mail.internet.MimeMessage;
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

            String title = "";
            String instructions = "";

            // Configurando remetente, destinatário e tópico do email
            helper.setFrom("contatesteapify1@gmail.com");
            helper.setTo(dto.email());

            // Essa parte vai depender do que vier no emailType
            if ("EMAIL_VERIFICATION".equals(dto.emailType())) {
                title = "Confirme seu Cadastro";
                instructions = "Estamos muito felizes em ter você no Come-come! Para ativar sua conta e começar a usar, use o código abaixo:";
            } else if ("PASSWORD_RESET".equals(dto.emailType())) {
                title = "Redefinição de Senha";
                instructions = "Recebemos um pedido para trocar sua senha. Se foi você, use o código abaixo para prosseguir:";
            }


            try(var inputStream = Objects.requireNonNull(EmailController.class.getResourceAsStream("/templates/email-content.html"))) {

                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                String finalHtml = template
                        .replace("{{TITLE}}", title)
                        .replace("{{MESSAGE}}", instructions)
                        .replace("{{TOKEN}}", dto.token());

                helper.setText(finalHtml, true);
            }

            // Envia o email
            helper.setSubject(title + " | Come-come");
            mailSender.send(message);
            System.out.println("Email enviado com sucesso para: " + dto.email());

        } catch (Exception e) {
            //Caso dê erro
            System.err.println("Erro ao processar mensagem da fila: " + e.getMessage());
            e.printStackTrace();

        }
    }
}

