package com.comecome.openfoodfacts.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuzzySearchService {

    private List<String> nomesProdutos;
    private static final LevenshteinDistance distancia = new LevenshteinDistance();

    @PostConstruct
    public void carregarNomes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/nomes_produtos.json");
            nomesProdutos = mapper.readValue(is, new TypeReference<List<String>>() {});
            System.out.println("Lista de produtos carregada: " + nomesProdutos.size() + " nomes.");

        } catch (Exception e) {
            System.err.println("Erro ao carregar nome");
            nomesProdutos = new ArrayList<>();
        }
    }

    public List<String> buscarSimilares(String tentativa, int limite) {
        if (nomesProdutos.isEmpty()) return Collections.emptyList();

        String entradaLower = tentativa.toLowerCase();

        // Calcula distância e também dá prioridade para os que contêm o termo
        return nomesProdutos.stream()
                .map(nome -> {
                    String nomeLower = nome.toLowerCase();
                    int dist = distancia.apply(entradaLower, nomeLower);
                    boolean contem = nomeLower.contains(entradaLower);
                    // Os que contêm o termo recebem um bônus (reduz a distância)
                    int score = contem ? dist - 3 : dist;
                    return Map.entry(nome, score);
                })
                .sorted(Map.Entry.comparingByValue())
                .limit(limite)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
