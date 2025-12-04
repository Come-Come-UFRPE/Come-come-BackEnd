package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.*;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.*;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewOpenFoodFactsService {

    private final ProdutoRepository produtoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            UiFilterService uiFilterService) {
        this.produtoRepository = produtoRepository;
        this.filteringResponseService = filteringResponseService;
        this.getAnamneseService = getAnamneseService;
        this.ingredientTranslationService = ingredientTranslationService;
        this.allergenTranslationService = allergenTranslationService;
        this.uiFilterService = uiFilterService;
    }

    public List<NewProductResponseDTO> buscarProdutos(String query, UUID userId, UiFilterDto uiFilter) {
        List<Produto> produtos = produtoRepository.buscarPorNomeOuMarca(query.trim());

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

        // Cria um produto temporário só pra usar seu sistema de filtros
        ProductResponseDto fakeProduct = criarProductResponseDtoTemporario(p, nutriments);

        List<String> violations = calcularViolacoes(fakeProduct, userId, uiFilter);

        // Ingredientes traduzidos
        List<NewIngredientDTO> ingredientes = extrairIngredientesTraduzidos(p.getIngredientsText());

        // Alérgenos traduzidos
        List<String> alergenos = extrairAlergenosTraduzidos(p.getIngredientsText());

        List<String> tags = extrairTags(p.getIngredientsTags());

        String nutriscore = p.getNutriscoreGrade() != null ? p.getNutriscoreGrade().toUpperCase() : "UNKNOWN";

        NewProductDetailsDTO details = new NewProductDetailsDTO(alergenos, ingredientes, nutriments, tags, nutriscore);

        return new NewProductResponseDTO(nome, imagem, p.getCode(), details, violations);
    }

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

        return new NewIngredientDTO(idFinal, traduzido);
    }

    private List<String> extrairAlergenosTraduzidos(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        String brutos = Arrays.stream(texto.toLowerCase().split("[,;\\.]"))
                .map(String::trim)
                .filter(s -> s.contains("leite") || s.contains("soja") || s.contains("ovo") ||
                            s.contains("amendoim") || s.contains("trigo") || s.contains("castanha") ||
                            s.contains("glúten") || s.contains("pescado") || s.contains("lactose"))
                .distinct()
                .collect(Collectors.joining(","));

        return brutos.isEmpty() ? List.of() : allergenTranslationService.translateAllergen(brutos);
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

    private ProductResponseDto criarProductResponseDtoTemporario(Produto p, Map<String, Object> nutriments) {
        ProductDetailsDto details = new ProductDetailsDto(
                List.of(),
                List.of(),
                null,
                nutriments,
                extrairTags(p.getIngredientsTags()),
                p.getNutriscoreGrade()
        );
        return new ProductResponseDto(p.getProductName(), p.getImageUrl(), details, List.of());
    }

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
}