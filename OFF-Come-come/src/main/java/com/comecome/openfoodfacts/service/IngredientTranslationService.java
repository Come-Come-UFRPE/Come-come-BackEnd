package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.utils.IngredientsMap;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class IngredientTranslationService {

    private final Map<String, String> translations = IngredientsMap.getMap();

    public String translate(String ingredientText) {
        if (ingredientText == null || ingredientText.isBlank()) {
            return ingredientText;
        }

        String lower = ingredientText.trim().toLowerCase(Locale.ROOT);
        String translated = translations.get(lower);

        return translated != null ? translated : capitalize(lower);
    }

    private String capitalize(String text) {
        if (text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
