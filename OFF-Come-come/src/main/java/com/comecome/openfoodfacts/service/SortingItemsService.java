package com.comecome.openfoodfacts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;


private final WebClient anamneseWebClient;
private final OpenFoodFactsService openFoodFactsService; // Injetando seu serviço!

@Autowired
public SortingItemsService(WebClient.Builder webClientBuilder,
                           OpenFoodFactsService openFoodFactsService) {

    this.openFoodFactsService = openFoodFactsService;

    this.anamneseWebClient = webClientBuilder
            .baseUrl("http://ANAMNESE") // Nome do Eureka
            .build();
}
}

