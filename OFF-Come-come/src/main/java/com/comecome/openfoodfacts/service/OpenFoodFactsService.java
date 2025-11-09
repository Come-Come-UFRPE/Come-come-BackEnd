package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.comecome.openfoodfacts.dtos.responseDtos.IngredientDto;
import com.comecome.openfoodfacts.dtos.responseDtos.NutrientLevelsDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductDetailsDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductResponseDto;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenFoodFactsService {


    private static final String BASE_URL = "https://world.openfoodfacts.org/cgi/search.pl"; //url base texto
    private static final String CODE_URL = "https://world.openfoodfacts.org/api/v2/product/"; //url barcode

    private final WebClient webClient;
    private final WebClient web2;

    @Autowired
    private AllergenTranslationService allergenTranslationService;

    @Autowired
    private IngredientTranslationService ingredientTranslationService;

    @Autowired
    private FilteringResponseService filteringResponseService;


    public OpenFoodFactsService(WebClient.Builder webClientBuilder) {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .exchangeStrategies(exchangeStrategies)
                .build();
        this.web2 = webClientBuilder
                .baseUrl(CODE_URL)
                .exchangeStrategies(exchangeStrategies).build();
    }

    public Mono<Map> searchProducts(String query, String countryCode, AnamnesePatchDto anamnese) { //montagem da url

        boolean isBarcode = query != null && query.matches("\\d+"); //regex verifica se a string é apenas numerica

        if (!isBarcode) {

        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder
                            .queryParam("search_terms", query)
                            .queryParam("search_simple", "1")
                            .queryParam("action", "process")
                            .queryParam("json", "1")
                            .queryParam("fields", "nutrient_levels,ingredients,nutriments,nutrition_grade_fr,allergens,image_front_url,product_name,ingredients_analysis_tags"); //limita só as coisas interessantes para nós

                    // Adiciona filtro de país apenas se fornecido
                    if (countryCode != null && !countryCode.trim().isEmpty()) {
                        builder.queryParam("countries_tags", countryCode);
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::translateAllergen)
                .map(apiResponse -> {
                    if (apiResponse == null || !apiResponse.containsKey("products")) {
                        return apiResponse;
                    }

                    List<Map<String, Object>> produtos = (List<Map<String, Object>>) apiResponse.get("products");

                    List<ProductResponseDto> produtosDto = produtos.stream()
                            // Filtra os produtos
                            .filter(produto ->
                                    produto.containsKey("ingredients") && // 1. Garante que a chave 'ingredients' existe
                                            produto.get("ingredients") != null && // 2. Garante que o valor da chave não é nulo
                                            !((List<?>) produto.get("ingredients")).isEmpty() // 3. (Opcional) Garante que a lista não é vazia
                            )
                            // Mapeia apenas os produtos que passaram pelo filtro
                            .map(produtoRaw -> this.toProductResponseDto(produtoRaw, List.of()))
                            .toList();

                    return Map.of("products", produtosDto);
                })
                .flatMap(mapOfProducts -> {
                        // Precisamos do Mono<Map> para o filteringResponse, mas aqui já é o Map desembrulhado.
                        // Vamos refatorar o FilteringResponseService para aceitar o Map<String, List<ProductResponseDto>>

                        // Se você não quer mudar a assinatura do filteringResponseService:
                    Mono<Map<String, List<ProductResponseDto>>> monoWithCorrectType = Mono.just(mapOfProducts);

                    return filteringResponseService.filteringResponse(monoWithCorrectType, anamnese);
                    });}
        else{

            return web2.get() //busca por barcode
                    .uri(query + ".json")
                    .retrieve()
                    .bodyToMono(Map.class);

        }
    }

    //* Método utilitário para traduzir alérgenos em uma resposta da API
    private Map translateAllergen(Map apiResponse) {
        if (apiResponse == null || !apiResponse.containsKey("products")) {
            return apiResponse;
        }

        List<Map<String, Object>> produtos = (List<Map<String, Object>>) apiResponse.get("products");

        for (Map<String, Object> produto : produtos) {
            Object allergens = produto.get("allergens");

            if (allergens instanceof String) {
                String allergensStr = (String) allergens;
                List<String> traduzidos = allergenTranslationService.translateAllergen(allergensStr);

                // Substitui no map 
                produto.put("allergens", traduzidos);
            }
        }

        return apiResponse;
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
        String translatedText = ingredientTranslationService.translate(text);

        return new IngredientDto(
                (String) map.get("id"),
                map.get("percent_estimate") != null ? map.get("percent_estimate").toString() : null,
                translatedText,
                (String) map.get("vegan"),
                (String) map.get("vegetarian"),
                subIngredients
        );
    }



    private ProductResponseDto toProductResponseDto(Map<String, Object> produto, List<String> initialViolations) {
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
        Map<String, Object> nutriments = nutrimentsRaw != null ? new HashMap<String, Object>(nutrimentsRaw) : null;

        List<String> allergens = null;
        if (produto.get("allergens") instanceof List<?>) {
            allergens = ((List<?>) produto.get("allergens"))
                .stream()
                .map(Object::toString)
                .toList();
        }

        //Ingredients tags
        Object rawIngredientTag = produto.get("ingredients_analysis_tags");
        List<String> ingredientAnalysisTags = null;

        if (rawIngredientTag instanceof List<?>) {
            // Agora faz o cast para Lista e mapeia os elementos para String
            ingredientAnalysisTags = ((List<?>) rawIngredientTag)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        String nutritionGrade = (String) produto.get("nutrition_grade_fr");

        ProductDetailsDto details = new ProductDetailsDto(allergens, ingredients, nutrientLevels, nutriments, ingredientAnalysisTags, nutritionGrade);
        return new ProductResponseDto(name, image, details,initialViolations);
    }


}
