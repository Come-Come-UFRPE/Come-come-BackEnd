package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.*;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class NewOpenFoodFactsService {

    private final ProdutoRepository produtoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NewOpenFoodFactsService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<NewProductResponseDTO> buscarProdutos(String query) {
        return produtoRepository.buscarPorNomeOuMarca(query.trim()).stream()
                .map(this::mapearParaDTOPerfeito)
                .toList();
    }

    private NewProductResponseDTO mapearParaDTOPerfeito(Produto p) {
        return new NewProductResponseDTO(
                limparNome(p.getProductName()),
                limparImagem(p.getImageUrl()),
                p.getCode(),
                criarDetalhesInteligentes(p),
                detectarViolations(p)
        );
    }

    private String limparNome(String nome) {
        return nome != null && !nome.isBlank() && !"NaN".equalsIgnoreCase(nome)
                ? nome.trim() : "Produto sem nome";
    }

    private String limparImagem(String url) {
        return url != null && url.startsWith("http") && !url.contains("NaN") ? url : null;
    }

    private NewProductDetailsDTO criarDetalhesInteligentes(Produto p) {
        return new NewProductDetailsDTO(
                extrairAlergenosLimpos(p.getIngredientsText()),
                extrairIngredientesLimpos(p.getIngredientsText()),
                extrairNutrimentsDoBanco(p.getNutriments()), // AGORA VEM DO BANCO!
                extrairTagsLimpos(p.getIngredientsTags()),
                formatarNutriscore(p.getNutriscoreGrade())
        );
    }

    private List<String> extrairAlergenosLimpos(String texto) {
        if (texto == null || texto.isBlank() || "NaN".equals(texto)) return List.of();
        return Stream.of("noisette", "lait", "soja", "arachide", "gluten", "œuf", "lactose")
                .filter(al -> texto.toLowerCase().contains(al))
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    private List<NewIngredientDTO> extrairIngredientesLimpos(String texto) {
        if (texto == null || texto.isBlank() || "NaN".equals(texto)) return List.of();

        return Arrays.stream(texto.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.matches("\\d+%?"))
                .map(s -> s.replaceAll("\\s+\\d+.*%", "").trim())
                .filter(s -> !s.isEmpty())
                .limit(10)
                .map(s -> new NewIngredientDTO(normalizarIngrediente(s)))
                .toList();
    }

    private String normalizarIngrediente(String ing) {
        return ing.toLowerCase()
                .replace("huile de palme", "óleo de palma")
                .replace("sucre", "açúcar")
                .replace("noix", "nozes")
                .replace("lécithines", "lecitina")
                .replace("cacao", "cacau");
    }

    private Map<String, Object> extrairNutrimentsDoBanco(String nutrimentsJson) {
        if (nutrimentsJson == null || nutrimentsJson.isBlank() || "unknown".equals(nutrimentsJson)) {
            return Map.of("energy-kcal_100g", 0, "fat_100g", 0.0, "sugars_100g", 0.0);
        }
        try {
            JsonNode node = objectMapper.readTree(nutrimentsJson);
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isNumber()) {
                    if (value.isIntegralNumber()) map.put(key, value.asLong());
                    else map.put(key, value.asDouble());
                } else if (value.isTextual()) {
                    map.put(key, value.asText());
                }
            });
            return map.isEmpty() ? Map.of("energy-kcal_100g", 0) : map;
        } catch (Exception e) {
            return Map.of("energy-kcal_100g", 0);
        }
    }

    private List<String> extrairTagsLimpos(String tags) {
        if (tags == null || tags.isBlank() || "NaN".equals(tags)) return List.of();
        return Arrays.stream(tags.split(","))
                .map(t -> t.startsWith("en:") ? t.substring(3) : t)
                .map(String::trim)
                .filter(t -> t.length() > 2)
                .limit(8)
                .toList();
    }

    private String formatarNutriscore(String grade) {
        return grade != null ? grade.toUpperCase() : "UNKNOWN";
    }

    private List<String> detectarViolations(Produto p) {
        List<String> v = new ArrayList<>();
        String ing = p.getIngredientsText() != null ? p.getIngredientsText().toLowerCase() : "";

        if (ing.contains("palm") || ing.contains("palme")) v.add("Contém óleo de palma");
        if ("E".equalsIgnoreCase(p.getNutriscoreGrade())) v.add("Nutri-Score E – Evitar");
        if ("D".equalsIgnoreCase(p.getNutriscoreGrade())) v.add("Nutri-Score D – Consumo moderado");
        if (ing.contains("additif") || ing.contains("e4") || ing.contains("e3")) v.add("Contém aditivos");
        if (p.getNovaGroup() != null && p.getNovaGroup() >= 4) v.add("Ultra-processado (NOVA 4)");

        return v;
    }
}