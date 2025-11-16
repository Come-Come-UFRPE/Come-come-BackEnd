package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.ComparePatchDTO;
import com.comecome.openfoodfacts.dtos.responseDtos.ComparingResponseDTO;
import com.comecome.openfoodfacts.service.ComparingService;
import com.comecome.openfoodfacts.dtos.FilteringDto;
import com.comecome.openfoodfacts.dtos.UiFilterDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.comecome.openfoodfacts.service.OpenFoodFactsService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/foods")
public class OpenFoodFactsController {


    private final OpenFoodFactsService openFoodFactsService;

    private final ComparingService comparingService;

    public OpenFoodFactsController(OpenFoodFactsService openFoodFactsService, ComparingService comparingService) {
        this.openFoodFactsService = openFoodFactsService;
        this.comparingService = comparingService;
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<?>> searchProducts(@Valid @RequestBody FilteringDto filteringDto) {
        //* Separa os DTOs dos filtros
        AnamneseSearchDTO search = filteringDto.search();
        UiFilterDto uiFilter = filteringDto.uiFilter(); // pode ser null

        return openFoodFactsService.searchProducts(
            search,
            "en:brazil",
            search.getUserID(),
            uiFilter
            ).map(ResponseEntity::ok);
    }

    @PostMapping("/compare")
    public Mono<ResponseEntity<ComparingResponseDTO>> compareProducts(@RequestBody ComparePatchDTO compare){
        return comparingService.comparingResponse(compare)
                .map(ResponseEntity::ok);
    }
}
