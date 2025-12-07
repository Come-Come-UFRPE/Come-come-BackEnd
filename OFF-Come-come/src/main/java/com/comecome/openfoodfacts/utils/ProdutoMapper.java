package com.comecome.openfoodfacts.utils;

import java.util.*;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.dtos.responseDtos.NutrientLevelsDto;
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

        // AGORA O CAMPO É CORRETAMENTE EXTRAÍDO DO BANCO
        NutrientLevelsDto nutrientLevelsDto = parseNutrientLevels(produto.getNutrientLevels());

        NewProductDetailsDTO detailsDTO = new NewProductDetailsDTO(
                alergenosTraduzidos,
                ingredientesTraduzidos,
                nutrimentsMap,
                nutrientLevelsDto,
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

    // Extrai tags separadas por vírgula
    private List<String> extrairTags(String tagsRaw) {
        if (tagsRaw == null || tagsRaw.isBlank()) return List.of();
        return Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    // NOVO: converte nutrient_levels_tags → NutrientLevelsDto
    private NutrientLevelsDto parseNutrientLevels(String tags) {

        if (tags == null || tags.isBlank()) return null;

        List<String> list = Arrays.stream(tags.split(","))
                .map(String::trim)
                .toList();

        String fat = null;
        String salt = null;
        String sat = null;
        String sugars = null;

        for (String tag : list) {
            if (tag.contains("fat-in-") && !tag.contains("saturated")) fat = tag;
            if (tag.contains("salt-in-")) salt = tag;
            if (tag.contains("saturated-fat-in-")) sat = tag;
            if (tag.contains("sugars-in-")) sugars = tag;
        }

        return new NutrientLevelsDto(
                fat,
                salt,
                sat,
                sugars
        );
    }
}
