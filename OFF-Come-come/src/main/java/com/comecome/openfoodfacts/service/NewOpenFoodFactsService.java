package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.*;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.*;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewOpenFoodFactsService {

    private final static String historico = "fila-historico";

    private final ProdutoRepository produtoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RabbitTemplate rabbitTemplate;


    // Seus serviços poderosos
    private final FilteringResponseService filteringResponseService;
    private final GetAnamneseService getAnamneseService;
    private final IngredientTranslationService ingredientTranslationService;
    private final AllergenTranslationService allergenTranslationService;
    private final UiFilterService uiFilterService;

    public NewOpenFoodFactsService(
            ProdutoRepository produtoRepository,
            FilteringResponseService filteringResponseService,
            GetAnamneseService getAnamneseService,
            IngredientTranslationService ingredientTranslationService,
            AllergenTranslationService allergenTranslationService,
            UiFilterService uiFilterService,
            RabbitTemplate rabbitTemplate) {
        this.produtoRepository = produtoRepository;
        this.filteringResponseService = filteringResponseService;
        this.getAnamneseService = getAnamneseService;
        this.ingredientTranslationService = ingredientTranslationService;
        this.allergenTranslationService = allergenTranslationService;
        this.uiFilterService = uiFilterService;
        this.rabbitTemplate = rabbitTemplate;
    }


    /* ******************************************************
     *                  FUNÇÕES PRINCIPAIS
     * ******************************************************
     */
    public List<NewProductResponseDTO> buscarProdutos(String query, UUID userId, UiFilterDto uiFilter) {
        if (isBarcode(query)) {
            Produto produto = produtoRepository.findByCode(query);
            if (produto != null) {
                return List.of(toNewProductResponseDTO(produto, userId, uiFilter));
            } else {
                return List.of(); // Produto não encontrado
            }
        }
        AnamneseSearchDTO sendHistorico = new AnamneseSearchDTO(userId,query);
        List<Produto> produtos = produtoRepository.buscarPorNomeOuMarca(query.trim());
        sendToRabbit(sendHistorico);

        return produtos.stream()
                .map(p -> toNewProductResponseDTO(p, userId, uiFilter))
                .toList();
    }


    public List<NewProductResponseDTO> buscarPorCategorias(String categories, UUID userID){
        List<Produto> produtos = produtoRepository.findByAnyCategory(categories);
        UiFilterDto uiFilter = new UiFilterDto(Set.of(), Set.of(), Set.of(), Set.of());
        return produtos.stream()
                .map(p -> toNewProductResponseDTO(p, userID, uiFilter))
                .toList();
    }


    private NewProductResponseDTO toNewProductResponseDTO(Produto p, UUID userId, UiFilterDto uiFilter) {
        String nome = limparNome(p.getProductName());
        String imagem = limparImagem(p.getImageUrl());
        Map<String, Object> nutriments = extrairNutriments(p.getNutriments());
        NutrientLevelsDto nutrientLevels = extrairNutrientLevels(p.getNutrientLevels());

        // Cria um produto temporário só pra usar seu sistema de filtros
        Map<String, Object> nutrimentsFake = extrairNutriments(p.getNutriments());
        ProductResponseDto fakeProduct = criarProductResponseDtoTemporario(p, nutrimentsFake, nutrientLevels);

        List<String> violations = calcularViolacoes(fakeProduct, userId, uiFilter);

        // Ingredientes traduzidos
        List<NewIngredientDTO> ingredientes = extrairIngredientesTraduzidos(p.getIngredientsText());

        // Alérgenos traduzidos
        List<String> alergenos = extrairAlergenosTraduzidos(p.getAllergens());

        List<String> tags = extrairTags(p.getIngredientsAnalysisTags());

        String nutriscore = p.getNutriscoreGrade() != null ? p.getNutriscoreGrade().toUpperCase() : "UNKNOWN";

        NewProductDetailsDTO details = new NewProductDetailsDTO(alergenos, ingredientes, nutriments, nutrientLevels, tags, nutriscore);

        return new NewProductResponseDTO(nome, imagem, p.getCode(), details, violations);
    }



    /* ******************************************************
     *          LIMPEZA E TRATAMENTO DE DADOS BRUTOS
     * ******************************************************
     */
    private List<NewIngredientDTO> extrairIngredientesTraduzidos(String texto) {
        if (texto == null || texto.isBlank() || "NaN".equalsIgnoreCase(texto.trim())) {
            return List.of();
        }

        return Arrays.stream(texto.split("[,;]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::removerPorcentagemEParentese)
                .filter(s -> s.length() > 1)
                .filter(s -> !s.matches(".*\\d+.*%?|\\d+"))
                .map(String::toLowerCase)
                .distinct()
                .limit(15)
                .map(this::traduzirComIdOriginal)
                .toList();
    }


    private String removerPorcentagemEParentese(String s) {
        return s.replaceAll("\\s*\\(?\\d+[.,]?\\d*\\s*%?\\)?", "")
                .replaceAll("[()]", "")
                .trim();
    }


    private NewIngredientDTO traduzirComIdOriginal(String original) {
        String idLimpo = original.replaceAll("[^a-zà-ú\\s-]", "").trim();
        String traduzido = ingredientTranslationService.translate(original);

        if (traduzido == null || traduzido.equalsIgnoreCase(idLimpo) || traduzido.isBlank()) {
            traduzido = ingredientTranslationService.translate(idLimpo);
        }

        traduzido = traduzido != null && !traduzido.isBlank()
                ? Character.toUpperCase(traduzido.charAt(0)) + traduzido.substring(1)
                : "Ingrediente desconhecido";

        String idFinal = idLimpo.isBlank() ? "unknown" : idLimpo.replaceAll("\\s+", "-");

        return new NewIngredientDTO(original, traduzido);
    }



    /* ******************************************************
     *                  EXTRAÇÃO DE DADOS
     * ******************************************************
     */
    private List<String> extrairAlergenosTraduzidos(String texto) {
        if (texto == null || texto.isBlank() || "NaN".equals(texto)) return List.of();
        String brutos = Arrays.stream(texto.toLowerCase().split("[,;\\.]"))
                .map(String::trim)
                .filter(s -> s.contains("en:milk") || s.contains("en:soybeans") || s.contains("en:egg") ||
                            s.contains("en:nuts") || s.contains("en:wheat") || s.contains("en:hazelnut") ||
                            s.contains("en:gluten") || s.contains("en:seafood") || s.contains("en:lactose"))
                .distinct()
                .collect(Collectors.joining(","));


        return allergenTranslationService.translateAllergen(brutos);
    }


    private List<String> extrairTags(String tags) {
        if (tags == null || tags.isBlank() || "NaN".equals(tags)) return List.of();
        return Arrays.stream(tags.split(","))
                .map(t -> t.replace("en:", "").trim())
                .filter(t -> t.length() > 2)
                .limit(12)
                .toList();
    }


    private Map<String, Object> extrairNutriments(String json) {
        if (json == null || json.isBlank() || "unknown".equals(json)) return Map.of();
        try {
            JsonNode node = objectMapper.readTree(json);
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(e -> {
                JsonNode v = e.getValue();
                if (v.isNumber()) map.put(e.getKey(), v.asDouble());
                else if (v.isTextual()) map.put(e.getKey(), v.asText());
            });
            return map;
        } catch (Exception e) {
            return Map.of();
        }
    }


    public NutrientLevelsDto extrairNutrientLevels(String input) {
        String fat = "unknown";
        String salt = "unknown";
        String saturatedFat = "unknown";
        String sugars = "unknown";

        if (input == null || input.trim().isEmpty()) {
            return new NutrientLevelsDto(fat, salt, saturatedFat, sugars);
        }


        String[] items = input.split(",");

        for (String item : items) {
            item = item.trim();

            if (item.startsWith("en:")) {
                item = item.substring(3);
            }

            if (item.contains("-in-")) {
                String[] parts = item.split("-in-");
                if (parts.length == 2) {
                    String nutrient = parts[0];
                    String level = parts[1].replace("-quantity", "");
                    switch (nutrient) {
                        case "fat" -> fat = level;
                        case "salt" -> salt = level;
                        case "saturated-fat" -> saturatedFat = level;
                        case "sugars" -> sugars = level;
                    }
                }
            }
        }
        return new NutrientLevelsDto(fat, salt, saturatedFat, sugars);
    }



    /* ******************************************************
     *                  CÁLCULO DE VIOLAÇÕES
     * ******************************************************
     */
    private List<String> calcularViolacoes(ProductResponseDto product, UUID userId, UiFilterDto uiFilter) {
        try {
            AnamnesePatchDto anamnese = userId != null
                    ? getAnamneseService.getAnamneseById(userId).blockOptional()
                    .orElse(new AnamnesePatchDto(null, Set.of(), Set.of(), Set.of()))
                    : new AnamnesePatchDto(null, Set.of(), Set.of(), Set.of());

            Map<String, Object> passo1 = filteringResponseService.filteringResponse(
                    Map.of("products", List.of(product)), anamnese);

            Map<String, Object> passo2 = (uiFilter != null && temFiltro(uiFilter))
                    ? uiFilterService.applyUiFilters(passo1, uiFilter)
                    : passo1;

            List<?> lista = (List<?>) passo2.get("products");
            if (lista == null || lista.isEmpty()) {
                return List.of("Produto incompatível com seu perfil de saúde");
            }

            ProductResponseDto resultado = (ProductResponseDto) lista.get(0);
            return resultado.violations() != null ? resultado.violations() : List.of();

        } catch (Exception e) {
            return List.of();
        }
    }



    /* ******************************************************
     *                  FUNÇÕES AUXILIARES
     * ******************************************************
     */
    private ProductResponseDto criarProductResponseDtoTemporario(Produto p, Map<String, Object> nutriments, NutrientLevelsDto nutrients) {
        List<NewIngredientDTO> dto = extrairIngredientesTraduzidos(p.getIngredientsTags());

        if (p.getNovaGroup() != null) {
            nutriments.put("nova_group", p.getNovaGroup());
        }

        List<IngredientDto> ingredientes =
                dto.stream()
                        .map(n -> new IngredientDto(
                                n.id(),          // id
                                null,            // percent_estimate
                                n.text(),        // text
                                null,            // vegan
                                null,            // vegetarian
                                List.of()        // ingredients (lista vazia ou null)
                        ))
                        .toList();
        ProductDetailsDto details = new ProductDetailsDto(
                List.of(),
                ingredientes,
                nutrients,
                nutriments,
                extrairTags(p.getIngredientsAnalysisTags()),
                p.getNutriscoreGrade()
        );

        System.out.println(details);
        return new ProductResponseDto(p.getProductName(), p.getImageUrl(), details, List.of());
    }

    private boolean temFiltro(UiFilterDto f) {
        if (f == null) return false;

        return (f.categories() != null && !f.categories().isEmpty()) ||
            (f.diets() != null && !f.diets().isEmpty()) ||
            (f.nutritional() != null && !f.nutritional().isEmpty()) ||
            (f.allergens() != null && !f.allergens().isEmpty());
    }

    private String limparNome(String nome) {
        return nome != null && !nome.trim().isBlank() && !"NaN".equalsIgnoreCase(nome)
                ? nome.trim() : "Produto sem nome";
    }

    private String limparImagem(String url) {
        return url != null && url.startsWith("http") && !url.contains("NaN") ? url : null;
    }

    private boolean isBarcode(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }
        
        // Apenas dígitos
        String digitsOnly = query.replaceAll("\\s+", "");
        
        // EAN-8: 8 dígitos e EAN-13: 13 dígitos
        return digitsOnly.matches("\\d+") && (digitsOnly.length() == 8 || digitsOnly.length() == 13);
    }

    private void sendToRabbit(AnamneseSearchDTO search) {
        AnamneseResponseDTO response = new AnamneseResponseDTO(
                search.getUserID(),
                search.getQuery(),
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(historico, response);
    }
}