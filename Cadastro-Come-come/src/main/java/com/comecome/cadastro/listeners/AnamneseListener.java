package com.comecome.cadastro.listeners;

import com.comecome.cadastro.dtos.AnamneseDTO;
import com.comecome.cadastro.services.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class AnamneseListener {

    public static final String fila = "anamnese-criada";

    final UserService userService;

    public AnamneseListener(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = fila)
    public void processar(AnamneseDTO message) {
        try{
            userService.atualizarStatusAnamnese(message.getUserID());
        }catch(Exception e){
            System.out.println("Erro ao atualizar anamnese");
        }
    }

}
