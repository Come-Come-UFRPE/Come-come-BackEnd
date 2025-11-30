package com.comecome.openfoodfacts.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.NewProductResponseDTO;
import com.comecome.openfoodfacts.service.NewOpenFoodFactsService;

@RestController
@RequestMapping("/api/foods")
public class NewOpenFoodFactsController {

    @Autowired
    private NewOpenFoodFactsService service;

    @GetMapping("/search2")
    public List<NewProductResponseDTO> buscar(@RequestParam String query) {
        return service.buscarProdutos(query.trim());
    }
}
