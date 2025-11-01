package com.comecome.openfoodfacts.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.comecome.openfoodfacts.service.FuzzySearchService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/foods/fuzzy-search")
public class FuzzySearchController {

    @Autowired
    private FuzzySearchService fuzzySearchService;

    @GetMapping
    public ResponseEntity<List<String>> fuzzySearch(@RequestParam String tentativa) {
        if (tentativa == null || tentativa.isBlank()) {
            return ResponseEntity.badRequest().body(List.of("Parâmetro 'tentativa' não pode estar vazio."));
        }

        List<String> resultados = fuzzySearchService.buscarSimilares(tentativa, 10);
        return ResponseEntity.ok(resultados);
    }
}
