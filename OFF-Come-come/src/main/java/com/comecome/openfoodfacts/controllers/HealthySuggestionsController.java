package com.comecome.openfoodfacts.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comecome.openfoodfacts.dtos.SuggestionsDTO;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.ProdutosResponseDTO;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.service.SuggestionsService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/foods")
public class HealthySuggestionsController {

    private final SuggestionsService suggestionsService;

    public HealthySuggestionsController(SuggestionsService suggestionsService){
        this.suggestionsService = suggestionsService;
    }
    
    @PostMapping("/suggestions")
    public ResponseEntity<ProdutosResponseDTO> healthySuggestions(@RequestBody SuggestionsDTO sugestao) {
        String query = sugestao.sugestao();
        
        ProdutosResponseDTO response = suggestionsService.getSugestoes(query);

        return ResponseEntity.ok(response);
    }
    
}
