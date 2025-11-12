package com.comecome.openfoodfacts.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductResponseDto;

@Service
public class UiFilterService {

    public Map<String, Object> applyUiFilters(Map<String, Object> map, UiFilterDto filters) {
        if (filters == null) {
            return map;
        }

        List<ProductResponseDto> produtos = (List<ProductResponseDto>) map.get("products");

        // UiFilterService.java
        List<ProductResponseDto> filtradosEAnatados = produtos.stream()
            .map(produto -> {
                if (!passaNosFiltros(produto, filters)) return null;

                List<String> violacoes = new ArrayList<>(produto.violations());
                violacoes.addAll(detectarViolacoesUi(produto, filters));
                Set<String> unicas = new LinkedHashSet<>(violacoes);

                return new ProductResponseDto(
                    produto.name(),
                    produto.image(),
                    produto.details(),
                    new ArrayList<>(unicas)
                );
            })
            .filter(Objects::nonNull)
            .toList();

        return Map.of("products", filtradosEAnatados);
    }

    // --- FILTRO: produto deve respeitar os filtros selecionados ---
    private boolean passaNosFiltros(ProductResponseDto p, UiFilterDto f) {
        var details = p.details();
        if (details == null) return true;

        // ==== CATEGORIAS (obrigatório) ====
        if (f.categories() != null && !f.categories().isEmpty() && details.ingredient_tags() != null) {
            boolean temCategoria = details.ingredient_tags().stream()
                    .anyMatch(tag -> f.categories().contains(tag.toLowerCase()));
            if (!temCategoria) return false;
        }

        // ==== DIETAS (vegetariano, kosher, etc.) ====
        if (f.diets() != null && !f.diets().isEmpty()) {
            boolean atendeDieta = f.diets().stream()
                    .allMatch(dieta -> atendePreferenciaDieta(p, dieta));
            if (!atendeDieta) return false;
        }

        // ==== NUTRICIONAL (baixo sódio, etc.) ====
        if (f.nutritional() != null && !f.nutritional().isEmpty()) {
            boolean atendeNutricional = f.nutritional().stream()
                    .allMatch(restricao -> atendeRestricaoNutricional(p, restricao));
            if (!atendeNutricional) return false;
        }

        // ==== ALÉRGICOS – NÃO FILTRAMOS AQUI ====
        // Só vamos detectar a violação depois, não remover o produto.

        return true;
    }

    // --- DETECÇÃO DE VIOLAÇÕES (igual à anamnese) ---
    private List<String> detectarViolacoesUi(ProductResponseDto p, UiFilterDto f) {
        List<String> violations = new ArrayList<>();

        var details = p.details();
        if (details == null) return violations;

        // 1. ALÉRGICOS – sempre verifica se o filtro está ativo
        if (f.allergens() != null && !f.allergens().isEmpty()) {
            f.allergens().forEach(alergeno -> {
                String violacao = switch (alergeno.toLowerCase()) {
                    case "gluten", "glúten"     -> checkIfContainsGluten(p)   ? "VIOLACAO_GLUTEN" : null;
                    case "lactose"              -> checkIfContainsLactose(p)  ? "VIOLACAO_LACTOSE" : null;
                    case "ovo", "ovos"          -> checkIfContainsEgg(p)      ? "VIOLACAO_OVO" : null;
                    case "amendoim"             -> checkIfContainsPeanuts(p)  ? "VIOLACAO_AMENDOIM" : null;
                    case "castanhas", "nozes"   -> checkIfContainsNuts(p)     ? "VIOLACAO_CASTANHAS_NOZES" : null;
                    case "peixe", "peixes"      -> checkIfContainsFish(p)     ? "VIOLACAO_PEIXES" : null;
                    case "frutos do mar", "frutos_do_mar" -> checkIfContainsSeaFood(p) ? "VIOLACAO_FRUTOS_DO_MAR" : null;
                    case "soja"                 -> checkIfContainsSoy(p)      ? "VIOLACAO_SOJA" : null;
                    default                     -> null;
                };
                if (violacao != null) violations.add(violacao);
            });
        }

        // 2. DIETAS
        if (f.diets() != null && !f.diets().isEmpty()) {
            f.diets().forEach(dieta -> {
                String violacao = switch (dieta.toLowerCase()) {
                    case "vegetariano" -> !checkVegetarian(p) ? "VIOLACAO_VEGETARIANA" : null;
                    case "vegano"      -> !checkVegan(p)       ? "VIOLACAO_VEGANA" : null;
                    case "kosher"      -> !checkKosher(p)      ? "VIOLACAO_KOSHER" : null;
                    case "halal"       -> !checkHalal(p)       ? "VIOLACAO_HALAL" : null;
                    default            -> null;
                };
                if (violacao != null) violations.add(violacao);
            });
        }

        // 3. NUTRICIONAL
        if (f.nutritional() != null && !f.nutritional().isEmpty()) {
            f.nutritional().forEach(restricao -> {
                String violacao = switch (restricao.toLowerCase()) {
                    case "baixo_sodio", "baixo em sódio" -> isHighSodium(p) ? "VIOLACAO_ALTO_SODIO" : null;
                    case "baixo_acucar", "baixo em açúcar" -> isHighSugar(p) ? "VIOLACAO_ALTO_ACUCAR" : null;
                    case "baixo_gordura", "baixo em gordura" -> isHighFat(p) ? "VIOLACAO_ALTA_GORDURA" : null;
                    default -> null;
                };
                if (violacao != null) violations.add(violacao);
            });
        }

        return violations;
    }

    // --- Funções de checagem (reaproveite ou crie) ---
    private boolean checkIfContainsGluten(ProductResponseDto p) {
        return containsIngredient(p, "en:wheat", "en:gluten", "gluten", "wheat");
    }

    private boolean checkIfContainsLactose(ProductResponseDto p) {
        return containsIngredient(p, "en:milk", "en:lactose", "en:whey", "en:casein", "milk", "lactose", "whey", "casein");
    }

    private boolean checkIfContainsEgg(ProductResponseDto p) {
        return containsIngredient(p, "en:egg", "en:albumen", "egg", "albumen");
    }

    private boolean checkIfContainsPeanuts(ProductResponseDto p) {
        return containsIngredient(p, "en:peanut", "peanut", "en:hazelnut");
    }

    private boolean checkIfContainsNuts(ProductResponseDto p) {
        return containsIngredient(p,
            "en:almond", "en:walnut", "en:cashew", "en:hazelnut", "en:pecan",
            "almond", "walnut", "cashew", "hazelnut", "pecan"
        );
    }

    private boolean checkIfContainsFish(ProductResponseDto p) {
        return containsIngredient(p, "en:fish", "fish");
    }

    private boolean checkIfContainsSeaFood(ProductResponseDto p) {
        return containsIngredient(p, "en:crustacean", "en:mollusc", "crustacean", "shrimp", "crab", "lobster");
    }

    private boolean checkIfContainsSoy(ProductResponseDto p) {
        return containsIngredient(p, "en:soybean", "en:soy", "soybean", "soy");
    }

    private boolean checkVegetarian(ProductResponseDto p) {
        return !containsIngredient(p,
            "en:meat", "en:chicken", "en:pork", "en:beef", "en:fish", "en:crustacean",
            "meat", "chicken", "pork", "beef", "fish", "shrimp"
        );
        // Ovo é permitido em vegetariano (ajuste se necessário)
    }

    private boolean checkVegan(ProductResponseDto p) {
        return checkVegetarian(p)
            && !checkIfContainsLactose(p)
            && !checkIfContainsEgg(p);
    }

    private boolean checkHalal(ProductResponseDto p) {
        return !containsIngredient(p, "en:pork", "en:gelatin", "en:alcohol", "pork", "gelatin", "alcohol");
    }

    private boolean checkKosher(ProductResponseDto p) {
        var details = p.details();
        if (details == null) return false;

        // 1. Verifica se tem tag "kosher" nos ingredient_tags
        if (details.ingredient_tags() != null) {
            boolean hasKosherTag = details.ingredient_tags().stream()
                .anyMatch(tag -> tag.toLowerCase().contains("kosher") || tag.equals("en:kosher"));
            if (hasKosherTag) return true;
        }

        // 2. Verifica se NÃO tem ingredientes proibidos
        boolean hasProhibited = containsIngredient(p,
            "en:pork", "en:gelatin", "en:lard", "en:bacon",
            "porco", "gelatina", "banha", "bacon"
        );

        return !hasProhibited;
    }

    private boolean isHighSodium(ProductResponseDto p) {
        Map<String, Object> nutriments = p.details().nutriments();
        if (nutriments == null) return false;

        Object saltObj = nutriments.get("salt");
        if (!(saltObj instanceof Number salt)) return false;

        return salt.doubleValue() > 1.5; // >1.5g/100g
    }

    private boolean isHighSugar(ProductResponseDto p) {
        Map<String, Object> nutriments = p.details().nutriments();
        if (nutriments == null) return false;

        Object sugarObj = nutriments.get("sugars");
        if (!(sugarObj instanceof Number sugar)) return false;

        return sugar.doubleValue() > 15; // >15g/100g
    }

    private boolean isHighFat(ProductResponseDto p) {
        Map<String, Object> nutriments = p.details().nutriments();
        if (nutriments == null) return false;

        Object fatObj = nutriments.get("fat");
        if (!(fatObj instanceof Number fat)) return false;

        return fat.doubleValue() > 20; // >20g/100g
    }

    private boolean containsIngredient(ProductResponseDto p, String... openFoodFactsIds) {
        if (p.details() == null || p.details().ingredients() == null) {
            return false;
        }

        return p.details().ingredients().stream()
            .anyMatch(ing -> {
                if (ing.id() == null) return false;
                String id = ing.id().toLowerCase();
                return Arrays.stream(openFoodFactsIds)
                    .anyMatch(expected -> id.equals(expected.toLowerCase()) || id.endsWith(":" + expected.toLowerCase()));
            });
    }

    private boolean atendePreferenciaDieta(ProductResponseDto p, String dieta) {
        return switch (dieta.toLowerCase()) {
            case "vegetariano" -> checkVegetarian(p);
            case "vegano" -> checkVegan(p);
            case "kosher" -> checkKosher(p);
            case "halal" -> checkHalal(p);
            default -> true;
        };
    }

    private boolean atendeRestricaoNutricional(ProductResponseDto p, String restricao) {
        return switch (restricao.toLowerCase()) {
            case "baixo_sodio" -> !isHighSodium(p);
            case "baixo_acucar" -> !isHighSugar(p);
            case "baixo_gordura" -> !isHighFat(p);
            default -> true;
        };
    }
}
