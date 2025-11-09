package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.responseDtos.AnamneseSearchDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.comecome.openfoodfacts.service.OpenFoodFactsService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/foods")
public class OpenFoodFactsController {


    private final OpenFoodFactsService openFoodFactsService;

    public OpenFoodFactsController(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<?>> searchProducts(@RequestBody AnamneseSearchDTO search) {
        return openFoodFactsService.searchProducts(search, "en:brazil").map(ResponseEntity::ok);
    }

}
