package com.comecome.openfoodfacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenfoodfactsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenfoodfactsApplication.class, args);

		// TODO 1: Mapear os alergenicos e suas traduções
		// TODO 2: Selecionar quais nutrientes são úteis ou não
		// TODO 3: Filtrar cada objeto de ingrediente para ter apenas esses valores: id, percent_estimate, vegan, vegetarian
	}

}
