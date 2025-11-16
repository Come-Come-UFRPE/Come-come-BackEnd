package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.*;
import com.comecome.openfoodfacts.exceptions.FoodNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private FilteringResponseService filteringResponseService;

    @Autowired
    private GetAnamneseService getAnamneseService;

    @Autowired
    private UiFilterService uiFilterService;

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

    public Mono<Map> searchProducts(
            AnamneseSearchDTO search,
            String countryCode,
            UUID userId, UiFilterDto uiFilter) {

        String query = search.getQuery();
        boolean isBarcode = query != null && query.matches("\\d+");
        WebClient client = isBarcode ? web2 : webClient;

        return client.get()
                .uri(uriBuilder -> buildUri(uriBuilder, query, countryCode, isBarcode))
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(new FoodNotFoundException("Produto não encontrado: " + query))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(apiResponse -> extractProducts(apiResponse, isBarcode))
                .flatMap(products -> {
                    if (products.get("products").isEmpty()) {
                        return Mono.error(new FoodNotFoundException("Nenhum produto encontrado para: " + query));
                    }
                    return Mono.just(products);
                })
                .doOnNext(products -> {
                    if (!isBarcode) {
                        sendToRabbit(search);
                    }
                })
                .flatMap(products -> applyFilters(products, search.getUserID(), uiFilter))
                .map(products -> (Map<String, List<ProductResponseDto>>) translateProducts(products));
    }

    private URI buildUri(UriBuilder uriBuilder, String query, String countryCode, boolean isBarcode) {
        UriBuilder builder = isBarcode
                ? uriBuilder.path(query + ".json")
                : uriBuilder
                .queryParam("search_terms", query)
                .queryParam("search_simple", "1")
                .queryParam("action", "process")
                .queryParam("json", "1");

        builder.queryParam("fields", "nutrient_levels,ingredients,nutriments,nutrition_grade_fr,allergens,image_front_url,product_name,ingredients_analysis_tags");

        if (countryCode != null && !countryCode.trim().isEmpty()) {
            builder.queryParam("countries_tags", countryCode);
        }

        return builder.build();
    }

    private Map<String, List<ProductResponseDto>> extractProducts(Map<String, Object> apiResponse, boolean isBarcode) {
        if (apiResponse == null) {
            return Map.of("products", List.of());
        }

        List<Map<String, Object>> rawProducts = isBarcode
                ? extractSingleProduct(apiResponse)
                : extractMultipleProducts(apiResponse);

        List<ProductResponseDto> validProducts = rawProducts.stream()
                .filter(this::hasValidIngredients)
                .map(product -> toProductResponseDto(product, List.of()))
                .toList();

        return Map.of("products", validProducts);
    }

    private List<Map<String, Object>> extractSingleProduct(Map<String, Object> apiResponse) {
        if (!apiResponse.containsKey("product")) {
            return List.of();
        }
        return List.of((Map<String, Object>) apiResponse.get("product"));
    }

    private List<Map<String, Object>> extractMultipleProducts(Map<String, Object> apiResponse) {
        if (!apiResponse.containsKey("products")) {
            return List.of();
        }
        return (List<Map<String, Object>>) apiResponse.get("products");
    }

    private boolean hasValidIngredients(Map<String, Object> product) {
        return product.containsKey("ingredients")
                && product.get("ingredients") instanceof List
                && !((List<?>) product.get("ingredients")).isEmpty();
    }

    private void sendToRabbit(AnamneseSearchDTO search) {
        AnamneseResponseDTO response = new AnamneseResponseDTO(
                search.getUserID(),
                search.getQuery(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(historico, response);
    }

    private Mono<Map<String, List<ProductResponseDto>>> applyFilters(
            Map<String, List<ProductResponseDto>> products,
            UUID userId,
            UiFilterDto uiFilter) {

        // Converte para Map<String, Object> que os services esperam
        Map<String, List<ProductResponseDto>> productsAsObject = new HashMap<>(products);

        return getAnamneseService.getAnamneseById(userId)
                .defaultIfEmpty(new AnamnesePatchDto(null, Set.of(), Set.of(), Set.of()))
                .map(anamnese -> {
                    Map<String, List<ProductResponseDto>> afterAnamnese = filteringResponseService
                            .filteringResponse(productsAsObject, anamnese);

                    Map<String, Object> afterUiFilter = uiFilterService
                            .applyUiFilters(afterAnamnese, uiFilter);

                    List<ProductResponseDto> filteredProducts =
                            (List<ProductResponseDto>) afterUiFilter.get("products");

                    return Map.of("products", filteredProducts);
                });
    }

    private Map<String, List<ProductResponseDto>> translateProducts(Map<String, List<ProductResponseDto>> apiResponse) {
        if (apiResponse == null || !apiResponse.containsKey("products")) {
            return apiResponse;
        }

        List<ProductResponseDto> produtos = apiResponse.get("products").stream()
                .map(produto -> {
                    ProductDetailsDto details = produto.details();

                    // Traduz alérgenos
                    List<String> translatedAllergens = details.allergens() != null
                            ? allergenTranslationService.translateAllergen(String.join(",", details.allergens()))
                            : null;

                    // Traduz ingredientes
                    List<IngredientDto> translatedIngredients = details.ingredients() != null
                            ? details.ingredients().stream()
                                .map(ing -> new IngredientDto(
                                        ing.id(),
                                        ing.percent_estimate(),
                                        ingredientTranslationService.translate(ing.text()),
                                        ing.vegan(),
                                        ing.vegetarian(),
                                        ing.ingredients()
                                ))
                                .toList()
                            : null;

                    ProductDetailsDto newDetails = new ProductDetailsDto(
                            translatedAllergens,
                            translatedIngredients,
                            details.nutrient_levels(),
                            details.nutriments(),
                            details.ingredient_tags(),
                            details.nutrition_grade_fr()
                    );

                    return new ProductResponseDto(
                            produto.name(),
                            produto.image(),
                            newDetails,
                            produto.violations()
                    );
                })
                .toList();

        return Map.of("products", produtos);
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
        Object allergensRaw = produto.get("allergens");
        if (allergensRaw instanceof String allergensStr) {
            // "en:milk,en:nuts"
            allergens = List.of(allergensStr.split(","));
        } else if (allergensRaw instanceof List<?>) {
            allergens = ((List<?>) allergensRaw)
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
