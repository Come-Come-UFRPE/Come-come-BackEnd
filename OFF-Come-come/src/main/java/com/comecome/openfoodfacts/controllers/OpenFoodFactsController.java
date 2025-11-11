package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import com.comecome.openfoodfacts.dtos.UserSearchDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.comecome.openfoodfacts.service.OpenFoodFactsService;

import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
public class OpenFoodFactsController {


    private final OpenFoodFactsService openFoodFactsService;

    public OpenFoodFactsController(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<?>> searchProducts(@RequestParam String query, @RequestBody UserSearchDto anamneseResponseDto) {
        return openFoodFactsService.searchProducts(query,"en:brazil", anamneseResponseDto.userID()).map(ResponseEntity::ok);
    }

}
