package com.comecome.openfoodfacts.utils;

import java.util.*;
import java.util.stream.Collectors;

import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.*;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProdutoMapper {

    private final ObjectMapper objectMapper;

    public ProdutoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NewProductResponseDTO toNewProductDTO(
            Produto produto,
            List<NewIngredientDTO> ingredientesTraduzidos,
            List<String> alergenosTraduzidos,
            List<String> violacoes
    ) {

        Map<String, Object> nutrimentsMap = new HashMap<>();
        try {
            if (produto.getNutriments() != null) {
                nutrimentsMap = objectMapper.readValue(produto.getNutriments(), Map.class);
            }
        } catch (Exception e) {
            nutrimentsMap = Map.of();
        }

        NewProductDetailsDTO detailsDTO = new NewProductDetailsDTO(
                alergenosTraduzidos,
                ingredientesTraduzidos,
                nutrimentsMap,
                extrairTags(produto.getIngredientsTags()),
                produto.getNutriscoreGrade()
        );

        return new NewProductResponseDTO(
                produto.getProductName(),
                produto.getImageUrl(),
                produto.getCode(),
                detailsDTO,
                violacoes
        );
    }

    private List<String> extrairTags(String tagsRaw) {
        if (tagsRaw == null || tagsRaw.isBlank()) return List.of();
        return Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}