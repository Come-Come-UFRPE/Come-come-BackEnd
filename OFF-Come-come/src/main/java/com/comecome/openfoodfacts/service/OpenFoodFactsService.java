package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.responseDtos.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenFoodFactsService {


    private static final String BASE_URL = "https://world.openfoodfacts.org/cgi/search.pl"; //url base texto
    private static final String CODE_URL = "https://world.openfoodfacts.org/api/v2/product/"; //url barcode
    private static final String historico = "fila-historico";

    private final WebClient webClient;
    private final WebClient web2;

    @Autowired
    private AllergenTranslationService allergenTranslationService;

    @Autowired
    private IngredientTranslationService ingredientTranslationService;

    private final RabbitTemplate rabbitTemplate;


    public OpenFoodFactsService(WebClient.Builder webClientBuilder, RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;

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

    public Mono<Map> searchProducts(AnamneseSearchDTO search, String countryCode) { //montagem da url

        String query = search.getQuery();

        boolean isBarcode = query != null && query.matches("\\d+"); //regex verifica se a string é apenas numerica

        if (!isBarcode) {

        return webClient.get()
                .uri(uriBuilder -> {
                    UriBuilder builder = uriBuilder
                            .queryParam("search_terms", query)
                            .queryParam("search_simple", "1")
                            .queryParam("action", "process")
                            .queryParam("json", "1")
                            .queryParam("fields", "nutrient_levels,ingredients,nutriments,nutrition_grade_fr,allergens,image_front_url,product_name"); //limita só as coisas interessantes para nós

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
                            .map(this::toProductResponseDto)
                            .toList();

                    search.setDataDaBusca(LocalDateTime.now());

                    rabbitTemplate.convertAndSend(historico, search);
                    System.out.println("Enviado para fila");


                    return Map.of("products", produtosDto);
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



    private ProductResponseDto toProductResponseDto(Map<String, Object> produto) {
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
        String nutritionGrade = (String) produto.get("nutrition_grade_fr");

        ProductDetailsDto details = new ProductDetailsDto(allergens, ingredients, nutrientLevels, nutriments, nutritionGrade);
        return new ProductResponseDto(name, image, details);
    }


}
