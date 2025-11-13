package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.responseDtos.SortingItemDto;
import com.comecome.openfoodfacts.service.SortingItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sorted-items") // O endpoint que o frontend vai chamar
public class SortingItemsController {

    @Autowired
    private SortingItemsService sortingItemsService;

    @GetMapping
    public ResponseEntity<Page<SortingItemDto>> getSortedItems(
            @RequestParam("filtro") String filtro,
            @RequestParam(value = "countryCode", defaultValue = "br") String countryCode,
            Pageable pageable
    ) {

//TODO PEGAR UUID DO USER AQUI

        // SIMULAÇÃO
        UUID userId = UUID.fromString("COLOQUE-UM-UUID-DE-TESTE-AQUI");

        Page<SortingItemDto> pagina = sortingItemsService.findAndSortItems(userId, filtro, countryCode, pageable);

        return ResponseEntity.ok(pagina);
    }
}