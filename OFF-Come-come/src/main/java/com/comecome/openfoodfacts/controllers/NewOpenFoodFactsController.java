package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.NewProductResponseDTO;
import com.comecome.openfoodfacts.service.NewOpenFoodFactsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/foods")
public class NewOpenFoodFactsController {

    private final NewOpenFoodFactsService service;

    public NewOpenFoodFactsController(NewOpenFoodFactsService service) {
        this.service = service;
    }

    @PostMapping("/search/v2")
    public ResponseEntity<List<NewProductResponseDTO>> buscar(@RequestBody SearchRequest request) {

        List<NewProductResponseDTO> resultado = service.buscarProdutos(
                request.search.query,
                request.search.userID,
                request.uiFilter
        );

        return ResponseEntity.ok(resultado);
    }

    record SearchRequest(
            SearchPayload search,
            UiFilterDto uiFilter
    ) {}

    record SearchPayload(
            UUID userID,
            String query
    ) {}
}