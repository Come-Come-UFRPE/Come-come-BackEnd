package com.comecome.openfoodfacts.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class IngredientsMap {

    private static Map<String, String> INGREDIENTS = new HashMap<>();

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = IngredientsMap.class.getResourceAsStream("/ingredientes_traduzidos_selenium.json");
            if (is != null) {
                INGREDIENTS = mapper.readValue(is, new TypeReference<Map<String, String>>() {});
                System.out.println("Ingredientes carregados: " + INGREDIENTS.size());
            } else {
                System.err.println("Arquivo ingredientes_traduzidos_selenium.json não encontrado no classpath!");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar ingredientes_traduzidos_selenium.json: " + e.getMessage());
        }
    }

    public static Map<String, String> getMap() {
        return INGREDIENTS;
    }
}
