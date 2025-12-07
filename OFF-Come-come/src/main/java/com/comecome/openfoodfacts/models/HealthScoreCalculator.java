package com.comecome.openfoodfacts.models;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class HealthScoreCalculator {

    public double calculate(Produto p){

        double score = 0.0;

        // 1. nutriscore_score
        double nutri = safeDouble(p.getNutriscoreScore());
        double nutriNormalized = 1 - ((nutri + 15.0) / 55.0);
        score += 0.5 * nutriNormalized;

        // 2. nova_group
        double nova = safeDouble(p.getNovaGroup());
        double novaNormalized = 1 - ((nova - 1) / 3.0);
        score += 0.3 * novaNormalized;

        // 3. nutrient_levels_tags
        score += 0.15 * nutrientPenalty(toList(p.getNutrientLevelsTags()));

        // 4. ingredients_analysis_tags
        score += 0.05 * ingredientPenalty(toList(p.getIngredientsAnalysisTags()));

        return score;
    }

    private List<String> toList(Object tags){
        if (tags == null) return List.of();

        if (tags instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }

        if (tags instanceof String[] arr){
            return Arrays.asList(arr);
        }

        if (tags instanceof String s){
            // Pode ser CSV ou JSON — trate como preferir
            return List.of(s);
        }

        return List.of();
    }

    private double nutrientPenalty(List<String> tags){
        if(tags == null || tags.isEmpty()) return 1.0;

        double penalty = 1.0;
        if(tags.contains("en:fat-in-moderate-quantity")) penalty -= 0.1;
        if(tags.contains("en:fat-in-high-quantity")) penalty -= 0.2;
        if(tags.contains("en:sugars-in-moderate-quantity")) penalty -= 0.1;
        if(tags.contains("en:sugars-in-high-quantity")) penalty -= 0.2;
        if(tags.contains("en:salt-in-moderate-quantity")) penalty -= 0.1;
        if(tags.contains("en:salt-in-high-quantity")) penalty -= 0.2;
        return penalty;
    }

    private double ingredientPenalty(List<String> tags){
        if(tags == null || tags.isEmpty()) return 1.0;

        double score = 1.0;
        if(tags.contains("en:palm-oil")) score -= 0.2;
        if(tags.contains("en:additives")) score -= 0.3;
        return score;
    }

    private double safeDouble(Object value){
        if (value == null) return 0.0;

        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e){
            return 0.0;
        }
    }
}


