package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.utils.AllergenMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
//* Retorna uma lista de alérgenos traduzidos a partir de uma string de alérgenos separados por vírgula
public class AllergenTranslationService {
    
    public List<String> translateAllergen(String allergensString) {
        
        return Arrays.stream(allergensString.split(","))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .map(AllergenMap::translate)
                .collect(Collectors.toList());
    }
}
