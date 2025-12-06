package com.comecome.openfoodfacts.controllers;

import com.comecome.openfoodfacts.dtos.CategoriesDto;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.NewProductResponseDTO;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;
import com.comecome.openfoodfacts.service.NewOpenFoodFactsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/foods")
public class NewOpenFoodFactsController {

    private final NewOpenFoodFactsService service;
    private final ProdutoRepository repository;

    public NewOpenFoodFactsController(NewOpenFoodFactsService service, ProdutoRepository repository) {
        this.service = service;
        this.repository = repository;
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

    @PostMapping("/search/categories")
    public ResponseEntity<List<NewProductResponseDTO>> buscarCategorias(@RequestBody CategoriesDto request) {

        List<NewProductResponseDTO> resultado = service.buscarPorCategorias(
                request.categories(),
                request.userID()
        );

        return ResponseEntity.ok(resultado);
    }
    @PostMapping("/search/v2/test/query")
    public ResponseEntity<List<Produto>> queryDb(@RequestBody String query) {
        
        List<Produto> resultado = repository.buscarPorNomeOuMarca(query);

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