package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.responseDtos.ComparisonResponseDto;
import com.comecome.openfoodfacts.service.ComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/compare")
public class ComparisonController {

    @Autowired
    private ComparisonService comparisonService;

    @GetMapping
    public ResponseEntity<ComparisonResponseDto> compareProducts(
            // O frontend envia os IDs que ele pegou da lista
            @RequestParam("idA") String idA,
            @RequestParam("idB") String idB
    ) {

//TODO COLOCAR O USERID AQUI
        UUID userId = UUID.fromString();

        ComparisonResponseDto response = comparisonService.compareProducts(userId, idA, idB);

        return ResponseEntity.ok(response);
    }
}