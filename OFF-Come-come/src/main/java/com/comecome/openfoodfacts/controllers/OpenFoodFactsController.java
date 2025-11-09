package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
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

    @GetMapping("/search")
    public Mono<ResponseEntity<?>> searchProducts(@RequestParam String query, @RequestBody AnamnesePatchDto anamneseResponseDto) {
        return openFoodFactsService.searchProducts(query,"en:brazil", anamneseResponseDto).map(ResponseEntity::ok);
    }

}
