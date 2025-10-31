package com.comecome.anamnese;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnamneseApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnamneseApplication.class, args);
	}
	// TODO 1: Estruturar o model principal, anamnese, que seria uma junção de vários outros models: 1 - Alergias Alimentares (Um pra muitos) | 2 - Condições de Saúde (Um pra muitos) | 3 - Dietas (Um pra muitos)
	// TODO 2: Criação do model: Perfil Nutricional -> One to One com o model de Anamnese | Cálculo de IMC somente por agora
	// TODO 3: Instanciar endpoints do microsserviço | 1 - POST createAnamnese | 2 - GET getAnamnese | 3 - PATCH updateAnamnese
	// TODO 4: Organizar services e controllers

}
