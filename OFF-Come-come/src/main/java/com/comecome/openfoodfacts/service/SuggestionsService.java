package com.comecome.openfoodfacts.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.comecome.openfoodfacts.models.HealthScoreCalculator;
import com.comecome.openfoodfacts.models.Produto;
import com.comecome.openfoodfacts.repositories.ProdutoRepository;

@Service
public class SuggestionsService {
    // TODO: Consultar por barcode para obter o produto
    // TODO: Chamar um método avaliador de healthyscore

    private final ProdutoRepository produtoRepository;
    private final HealthScoreCalculator healthScoreCalculator;

    public SuggestionsService(ProdutoRepository produtoRepository, HealthScoreCalculator healthScoreCalculator){
        this.produtoRepository = produtoRepository;
        this.healthScoreCalculator = healthScoreCalculator;
    }

    public List<Produto> getSugestoes(String code){

        Produto original = produtoRepository.findByCode(code);
        if(original == null) return List.of();

        List<Produto> similares =
                produtoRepository.findByAnyCategory(original.getCategories());

        // Inclui o produto original na comparação
        similares.add(original);

        // Ordena pelo healthScore
        return similares.stream()
                .sorted((a,b) -> Double.compare(
                        healthScoreCalculator.calculate(b),
                        healthScoreCalculator.calculate(a)
                ))
                .limit(3)
                .toList();
    }
}
