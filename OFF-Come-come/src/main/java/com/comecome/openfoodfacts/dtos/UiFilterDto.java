package com.comecome.openfoodfacts.dtos;

import java.util.*;

public record UiFilterDto(
        Set<String> selectedCategories,
        Set<String> allergies,
        Set<String> dietaryPreferences,
        Map<String, String> nutritionLevels
) {
    public UiFilterDto {
        selectedCategories = orEmpty(selectedCategories);
        allergies = orEmpty(allergies);
        dietaryPreferences = orEmpty(dietaryPreferences);
        nutritionLevels = orEmptyMap(nutritionLevels);
    }

    /*
     * Retorna set/map vazio pra evitar nullpointer exception
     */

    private static <T> Set<T> orEmpty(Set<T> set) {
        return set != null ? Set.copyOf(set) : Set.of();
    }

    private static <K, V> Map<K, V> orEmptyMap(Map<K, V> map) {
        return map != null ? Map.copyOf(map) : Map.of();
    }
}