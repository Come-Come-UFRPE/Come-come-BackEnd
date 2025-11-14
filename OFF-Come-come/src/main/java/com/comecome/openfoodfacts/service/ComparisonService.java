package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.gateway.AnamneseDto;
import com.comecome.openfoodfacts.dtos.responseDtos.*; // Importa seus DTOs (ProductResponseDto, ProductDetailsDto, etc)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComparisonService {

    // Dependências necessárias
    private final OpenFoodFactsService openFoodFactsService;
    private final WebClient anamneseWebClient;
    private final IngredientTranslationService ingredientTranslationService; // Copiado do seu OpenFoodFactsService

    @Autowired
    public ComparisonService(OpenFoodFactsService openFoodFactsService,
                             WebClient.Builder webClientBuilder,
                             IngredientTranslationService ingredientTranslationService) { // Injeta o tradutor

        this.openFoodFactsService = openFoodFactsService;
        this.ingredientTranslationService = ingredientTranslationService;

        // Cria o cliente para o serviço de Anamnese
        this.anamneseWebClient = webClientBuilder
                .baseUrl("http://ANAMNESE") // Nome do serviço no Eureka
                .build();
    }

    /**
     * Busca a Anamnese e os dois produtos (A e B) em paralelo,
     * e então os "funde" em SortingItemDTOs.
     */
    public ComparisonResponseDto compareProducts(UUID userId, String idA, String idB) {

        // --- 1. PREPARAÇÃO DAS CHAMADAS ---

        // Chamada 1: Buscar a Anamnese
        Mono<AnamneseDto> anamneseMono = anamneseWebClient.get()
                .uri("/api/anamnese/user/" + userId.toString()) // Confirme este path
                .retrieve()
                .bodyToMono(AnamneseDto.class);

        // Chamada 2: Buscar Produto A (usa nosso novo helper)
        Mono<ProductResponseDto> productAMono = getProductById(idA);

        // Chamada 3: Buscar Produto B (usa nosso novo helper)
        Mono<ProductResponseDto> productBMono = getProductById(idB);

        // --- 2. EXECUÇÃO EM PARALELO ---
        // Mono.zip() executa as 3 chamadas ao mesmo tempo e espera todas terminarem
        Tuple3<AnamneseDto, ProductResponseDto, ProductResponseDto> results =
                Mono.zip(anamneseMono, productAMono, productBMono).block();

        // --- 3. PROCESSAMENTO (A MÁGICA) ---
        AnamneseDto anamnese = results.getT1();
        ProductResponseDto productA = results.getT2();
        ProductResponseDto productB = results.getT3();

        // Reutilizamos o "Prato Final" (SortingItemDTO).
        // É aqui que a adequação (Verde/Amarelo/Vermelho) é calculada!
        SortingItemDto dtoA = new SortingItemDto(productA, anamnese);
        SortingItemDto dtoB = new SortingItemDto(productB, anamnese);

        // --- 4. RETORNO ---
        return new ComparisonResponseDto(dtoA, dtoB);
    }

    /**
     * Helper privado que chama seu 'searchProducts' e converte o 'Map' bruto.
     */
    private Mono<ProductResponseDto> getProductById(String id) {
        // 1. Chama seu método existente. O 'countryCode' é null, pois a busca por ID não usa.
        return openFoodFactsService.searchProducts(id, null)
                .map(apiResponse -> {
                    // 2. Extrai o mapa do produto
                    // A resposta de barcode é {"product": {...}}
                    Map<String, Object> productMap = (Map<String, Object>) apiResponse.get("product");

                    // 3. Converte o mapa para o DTO (lógica copiada)
                    return toProductResponseDto(productMap);
                });
    }

    // --- LÓGICA DE CONVERSÃO (Copiada do seu OpenFoodFactsService) ---
    // Tivemos que copiar pois ela era 'private' lá.

    private ProductResponseDto toProductResponseDto(Map<String, Object> produto) {
        String id = (String) produto.get("_id"); // ID que pedimos
        String name = (String) produto.getOrDefault("product_name", "Sem nome");
        String image = (String) produto.get("image_front_url");

        // Ingredientes
        List<Map<String, Object>> ingredientsRaw = (List<Map<String, Object>>) produto.get("ingredients");
        List<IngredientDto> ingredients = null;
        if (ingredientsRaw != null) {
            ingredients = ingredientsRaw.stream()
                    .map(this::toIngredientDto)
                    .toList();
        }

        // Nutrient levels
        Map<String, Object> nutrientLevelsRaw = (Map<String, Object>) produto.get("nutrient_levels");
        NutrientLevelsDto nutrientLevels = null;
        if (nutrientLevelsRaw != null) {
            nutrientLevels = new NutrientLevelsDto(
                    (String) nutrientLevelsRaw.get("fat"),
                    (String) nutrientLevelsRaw.get("salt"),
                    (String) nutrientLevelsRaw.get("saturated-fat"),
                    (String) nutrientLevelsRaw.get("sugars")
            );
        }

        // Nutriments
        Map<String, Object> nutrimentsRaw = (Map<String, Object>) produto.get("nutriments");
        Map<String, Object> nutriments = nutrimentsRaw != null ? new HashMap<>(nutrimentsRaw) : null;

        // Allergens
        List<String> allergens = null;
        if (produto.get("allergens") instanceof List<?>) {
            allergens = ((List<?>) produto.get("allergens"))
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        // Outros campos (para o SortingItemDTO)
        String nutritionGrade = (String) produto.get("nutrition_grade_fr");
        String veganStatus = (String) produto.get("vegan_status");
        String vegetarianStatus = (String) produto.get("vegetarian_status");

        // O campo nova_group é um Integer (1, 2, 3 ou 4)
        Integer novaGroup = null;
        Object novaRaw = produto.get("nova_group");
        if (novaRaw instanceof Number) {
            novaGroup = ((Number) novaRaw).intValue();
        } else if (novaRaw instanceof String) {
            try {
                novaGroup = Integer.parseInt((String) novaRaw);
            } catch (NumberFormatException e) {
                // ignora
            }
        }

        // Cria o 'details' (assumindo que ProductDetailsDto é um record)
        ProductDetailsDto details = new ProductDetailsDto(
                allergens, ingredients, nutrientLevels, nutriments,
                nutritionGrade, veganStatus, vegetarianStatus, novaGroup
        );

        return new ProductResponseDto(id, name, image, details);
    }

    private IngredientDto toIngredientDto(Map<String, Object> map) {
        List<Map<String, Object>> subIngredientsRaw = (List<Map<String, Object>>) map.get("ingredients");
        List<IngredientDto> subIngredients = null;
        if (subIngredientsRaw != null) {
            subIngredients = subIngredientsRaw.stream()
                    .map(this::toIngredientDto)
                    .toList();
        }

        String text = (String) map.get("text");
        String translatedText = ingredientTranslationService.translate(text); // Usa o serviço injetado

        return new IngredientDto(
                (String) map.get("id"),
                map.get("percent_estimate") != null ? map.get("percent_estimate").toString() : null,
                translatedText,
                (String) map.get("vegan"),
                (String) map.get("vegetarian"),
                subIngredients
        );
    }
}