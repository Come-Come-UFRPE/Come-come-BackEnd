package com.comecome.openfoodfacts.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.NewIngredientDTO;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.NewProductResponseDTO;
import com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs.ProdutosResponseDTO;
import com.comecome.openfoodfacts.models.HealthScoreCalculator;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;
import com.comecome.openfoodfacts.utils.ProdutoMapper;

@Service
public class SuggestionsService {

    private final ProdutoRepository produtoRepository;
    private final HealthScoreCalculator healthScoreCalculator;
    private final ProdutoMapper produtoMapper;
    private final IngredientTranslationService ingredientTranslationService;
    private final AllergenTranslationService allergenTranslationService;

    public SuggestionsService(  ProdutoRepository produtoRepository, 
                                HealthScoreCalculator healthScoreCalculator, 
                                ProdutoMapper produtoMapper,
                                IngredientTranslationService ingredientTranslationService,
                                AllergenTranslationService allergenTranslationService){
        this.produtoRepository = produtoRepository;
        this.healthScoreCalculator = healthScoreCalculator;
        this.produtoMapper = produtoMapper;
        this.ingredientTranslationService = ingredientTranslationService;
        this.allergenTranslationService = allergenTranslationService;
    }

    public ProdutosResponseDTO getSugestoes(String code) {

        Produto original = produtoRepository.findByCode(code);
        if (original == null) return new ProdutosResponseDTO(List.of());

        List<Produto> similares =
                produtoRepository.findByAnyCategory(original.getCategories());

        // Inclui o original para comparação
        similares.add(original);

        // Ordena pelo healthScore
        List<Produto> top3 = similares.stream()
                .sorted((a,b) -> Double.compare(
                        healthScoreCalculator.calculate(b),
                        healthScoreCalculator.calculate(a)
                ))
                .limit(3)
                .toList();

        // Converte para DTO
        List<NewProductResponseDTO> produtosDTO =
                top3.stream()
                    .map(p -> produtoMapper.toNewProductDTO(
                            p,
                            traduzirIngredientes(p),
                            extrairAlergenosTraduzidos(p.getAllergens()),
                            List.of() // as violações se você tiver um cálculo
                    ))
                    .toList();

        return new ProdutosResponseDTO(produtosDTO);
    }

    private List<NewIngredientDTO> traduzirIngredientes(Produto p) {
        if (p.getIngredientsText() == null) return List.of();

        return Arrays.stream(p.getIngredientsText().split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::traduzirComIdOriginal)
                .toList();
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
}
