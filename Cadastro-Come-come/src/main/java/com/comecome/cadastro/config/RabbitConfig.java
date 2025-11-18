package com.comecome.cadastro.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public static final String fila = "anamnese-criada";

    public static final String fila_email = "send-emails";

    @Bean
    public Queue anamneseQueue() {

        return new Queue(fila, true);

    }

    @Bean
    public Queue emailQueue() {

        return new Queue(fila_email, true);

    }
}
