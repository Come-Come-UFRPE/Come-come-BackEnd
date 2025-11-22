package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.models.RuleViolations;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UiFilterService {

    private Map<String, RuleViolations> rules;

    @PostConstruct
    public void loadRules() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/violationsUi.json");

            if (is == null) {
                throw new RuntimeException("Arquivo violationsUI.json não encontrado no classpath! Verifique se está em src/main/resources");
            }

            this.rules = mapper.readValue(is, new TypeReference<Map<String, RuleViolations>>() {});
            
        } catch (Exception e) {
            throw new RuntimeException("Erro carregando violationsUI.json", e);
        }
    }

    // -------------------------------------------------------------------------
    //  APLICAÇÃO GERAL DOS FILTROS
    // -------------------------------------------------------------------------

    public Map<String, Object> applyUiFilters(Map<String, Object> map, UiFilterDto filters) {
        if (filters == null) return map;

        List<ProductResponseDto> produtos = (List<ProductResponseDto>) map.get("products");

        List<ProductResponseDto> filtradosEAnotados = produtos.parallelStream()
                .map(produto -> {

                    if (!passaNosFiltros(produto, filters))
                        return null;

                    List<String> violacoes = new ArrayList<>(produto.violations());
                    violacoes.addAll(detectarViolacoesUi(produto, filters));

                    return new ProductResponseDto(
                            produto.name(),
                            produto.image(),
                            produto.details(),
                            new ArrayList<>(new LinkedHashSet<>(violacoes))
                    );
                })
                .filter(Objects::nonNull)
                .filter(produto -> produto.violations().isEmpty())
                .toList();

        return Map.of("products", filtradosEAnotados);
    }

    // -------------------------------------------------------------------------
    //  FILTRO DE PASSAGEM DO PRODUTO
    // -------------------------------------------------------------------------

    private boolean passaNosFiltros(ProductResponseDto p, UiFilterDto f) {
        var details = p.details();
        if (details == null) return true;

        // CATEGORIAS
        if (f.categories() != null && !f.categories().isEmpty()) {
            if (details.ingredient_tags() == null) return false;

            boolean temCategoria = details.ingredient_tags().stream()
                    .anyMatch(tag -> f.categories().contains(tag.toLowerCase()));

            if (!temCategoria) return false;
        }

        // DIETAS
        if (f.diets() != null && !f.diets().isEmpty()) {
            boolean ok = f.diets().stream()
                    .allMatch(d -> checkDiet(p, d.toLowerCase()));
            if (!ok) return false;
        }

        // NUTRICIONAL (mantido como estava)
        if (f.nutritional() != null && !f.nutritional().isEmpty()) {
            boolean ok = f.nutritional().stream()
                    .allMatch(r -> atendeRestricaoNutricional(p, r));
            if (!ok) return false;
        }

        return true;
    }

    // -------------------------------------------------------------------------
    //  DETECÇÃO DE VIOLAÇÕES — usando JSON
    // -------------------------------------------------------------------------

    private List<String> detectarViolacoesUi(ProductResponseDto p, UiFilterDto f) {
        List<String> violations = new ArrayList<>();

        // ALÉRGICOS
        if (f.allergens() != null) {
            for (String allergen : f.allergens()) {
                String key = allergen.toLowerCase();
                RuleViolations rule = rules.get(key);

                if (rule != null && "allergen".equals(rule.getType())) {
                    if (checkAllergen(p, key)) {
                        violations.add(rule.getViolation_code());
                    }
                }
            }
        }

        // DIETAS
        if (f.diets() != null) {
            for (String d : f.diets()) {
                String key = d.toLowerCase();
                RuleViolations rule = rules.get(key);

                if (rule != null && "diet".equals(rule.getType())) {
                    if (!checkDiet(p, key)) {
                        violations.add(rule.getViolation_code());
                    }
                }
            }
        }

        // NUTRICIONAL — mesma lógica original
        if (f.nutritional() != null) {
            for (String restricao : f.nutritional()) {
                String v = switch (restricao.toLowerCase()) {
                    case "baixo_sodio" -> isHighSodium(p) ? "VIOLACAO_ALTO_SODIO" : null;
                    case "baixo_acucar" -> isHighSugar(p) ? "VIOLACAO_ALTO_ACUCAR" : null;
                    case "baixo_gordura" -> isHighFat(p) ? "VIOLACAO_ALTA_GORDURA" : null;
                    default -> null;
                };
                if (v != null) violations.add(v);
            }
        }

        return violations;
    }

    // -------------------------------------------------------------------------
    //  CHECKS GENÉRICOS — LENDO AS REGRAS DO JSON
    // -------------------------------------------------------------------------

    private boolean checkAllergen(ProductResponseDto p, String allergenKey) {
        RuleViolations r = rules.get(allergenKey);
        if (r == null || r.getTags() == null) return false;

        return containsAnyTag(p, r.getTags());
    }

    private boolean checkDiet(ProductResponseDto p, String dietKey) {
        RuleViolations r = rules.get(dietKey);
        if (r == null) return true;

        // requires_tag
        if (r.getRequires_tag() != null) {
            List<String> tags = p.details().ingredient_tags();
            boolean has = tags != null && tags.stream()
                    .anyMatch(t -> t.equalsIgnoreCase(r.getRequires_tag()));

            if (!has) return false;
        }

        // depends_on
        if (r.getDepends_on() != null) {
            for (String dep : r.getDepends_on()) {

                if (rules.containsKey(dep)) {
                    RuleViolations childRule = rules.get(dep);

                    if ("diet".equals(childRule.getType()) && !checkDiet(p, dep)) {
                        return false;
                    }

                    if ("allergen".equals(childRule.getType()) && checkAllergen(p, dep)) {
                        return false;
                    }
                }
            }
        }

        // exclusores
        if (r.getExclusores() != null) {
            if (containsAnyTag(p, r.getExclusores())) {
                return false;
            }
        }

        return true;
    }

    // -------------------------------------------------------------------------
    //  FUNÇÃO GENÉRICA DE MATCH DE INGREDIENTES (Open Food Facts)
    // -------------------------------------------------------------------------

    private boolean containsAnyTag(ProductResponseDto p, List<String> tags) {
        if (p.details() == null || p.details().ingredients() == null) return false;

        return p.details().ingredients().stream().anyMatch(ing -> {
            if (ing.id() == null) return false;
            String id = ing.id().toLowerCase();
            return tags.stream().anyMatch(tag ->
                    id.equals(tag) || id.endsWith(":" + tag));
        });
    }

    // -------------------------------------------------------------------------
    //  CHECKS NUTRICIONAIS — mantidos como antes
    // -------------------------------------------------------------------------

    private boolean atendeRestricaoNutricional(ProductResponseDto p, String r) {
        return switch (r.toLowerCase()) {
            case "baixo_sodio" -> !isHighSodium(p);
            case "baixo_acucar" -> !isHighSugar(p);
            case "baixo_gordura" -> !isHighFat(p);
            default -> true;
        };
    }

    private boolean isHighSodium(ProductResponseDto p) {
        Map<String, Object> n = p.details().nutriments();
        if (n == null) return false;
        Object salt = n.get("salt");
        return salt instanceof Number s && s.doubleValue() > 1.5;
    }

    private boolean isHighSugar(ProductResponseDto p) {
        Map<String, Object> n = p.details().nutriments();
        if (n == null) return false;
        Object sugar = n.get("sugars");
        return sugar instanceof Number s && s.doubleValue() > 15;
    }

    private boolean isHighFat(ProductResponseDto p) {
        Map<String, Object> n = p.details().nutriments();
        if (n == null) return false;
        Object fat = n.get("fat");
        return fat instanceof Number f && f.doubleValue() > 20;
    }
}
