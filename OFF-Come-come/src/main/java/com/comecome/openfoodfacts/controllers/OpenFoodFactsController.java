package com.comecome.openfoodfacts.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comecome.openfoodfacts.service.OpenFoodFactsService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/foods")
public class OpenFoodFactsController {


    private final OpenFoodFactsService openFoodFactsService;

    public OpenFoodFactsController(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<?>> searchProducts(@RequestParam String query) {
        return openFoodFactsService.searchProducts(query,"en:brazil").map(ResponseEntity::ok);
    }

}
