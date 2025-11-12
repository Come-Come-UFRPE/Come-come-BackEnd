package com.comecome.openfoodfacts.dtos;

import java.util.*;

public record UiFilterDto(
        Set<String> categories,
        Set<String> allergens,
        Set<String> diets,
        Set<String> nutritional
) {
    public UiFilterDto {
        categories = orEmpty(categories);
        allergens = orEmpty(allergens);
        diets = orEmpty(diets);
        nutritional = orEmpty(nutritional);
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