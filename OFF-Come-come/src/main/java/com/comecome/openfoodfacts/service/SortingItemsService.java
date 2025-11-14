package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.gateway.AnamneseDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductResponseDto;
import com.comecome.openfoodfacts.dtos.responseDtos.SortingItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SortingItemsService {


    private final OpenFoodFactsService openFoodFactsService;
    private final WebClient anamneseWebClient;


    @Autowired
    public SortingItemsService(OpenFoodFactsService openFoodFactsService,
                               WebClient.Builder webClientBuilder) {

        this.openFoodFactsService = openFoodFactsService;

        this.anamneseWebClient = webClientBuilder
                .baseUrl("http://ANAMNESE")
                .build();
    }

    public Page<SortingItemDto> findAndSortItems(UUID userId, String filtro, String countryCode, Pageable pageable) {

        // CHAMADA 1: Buscar Anamnese
        AnamneseDto anamnese = anamneseWebClient.get()
                // Converte o UUID para String para a URL
                .uri("/api/anamnese/user/" + userId.toString()) //ENDPOINT A CONFIRMAR
                .retrieve()
                .bodyToMono(AnamneseDto.class)
                .block();

        // CHAMADA 2: Buscar Alimentos
        Map apiResponse = openFoodFactsService.searchProducts(filtro, countryCode).block();

        // Trata resposta vazia
        if (apiResponse == null || !apiResponse.containsKey("products") ||
                ((List<?>) apiResponse.get("products")).isEmpty()) {
            return Page.empty(pageable);
        }

        List<ProductResponseDto> produtosApi = (List<ProductResponseDto>) apiResponse.get("products");

        // PASSO 3: ANÁLISE DA ANAMNESE E PRODUTO
        List<SortingItemDto> listaCompleta = produtosApi.stream()
                .map(produto -> new SortingItemDto(produto, anamnese))
                .collect(Collectors.toList());

        // PASSO 4: ORDENAÇÃO (na Memória)
        Comparator<SortingItemDto> comparator = buildComparator(pageable.getSort());
        listaCompleta.sort(comparator);

        // PASSO 5: PAGINAÇÃO
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), listaCompleta.size());

        if (start > listaCompleta.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, listaCompleta.size());
        }

        List<SortingItemDto> paginaConteudo = listaCompleta.subList(start, end);

        // PASSO 6: Retorno
        return new PageImpl<>(paginaConteudo, pageable, listaCompleta.size());
    }

    private Comparator<SortingItemDto> buildComparator(Sort sort) {

        // Pega a primeira regra de sort. Se nenhuma for passada, usa "adequacao,asc" como padrão.
        Sort.Order sortOrder = sort.stream().findFirst()
                .orElse(Sort.Order.asc("adequacao")); // Padrão

        String property = sortOrder.getProperty();
        Comparator<SortingItemDto> comparator;

        // Compara com base na propriedade pedida
        switch (property) {
            case "nome":
                comparator = Comparator.comparing(SortingItemDto::getNome, String.CASE_INSENSITIVE_ORDER);
                break;
            case "valorEnergetico":
                // nullsLast: joga os produtos sem informação nutricional para o fim da lista
                comparator = Comparator.comparing(SortingItemDto::getValorEnergetico, Comparator.nullsLast(Double::compareTo));
                break;
            case "proteinas":
                comparator = Comparator.comparing(SortingItemDto::getProteinas, Comparator.nullsLast(Double::compareTo));
                break;
            case "sodio":
                comparator = Comparator.comparing(SortingItemDto::getSodio, Comparator.nullsLast(Double::compareTo));
                break;
            case "acucares":
                comparator = Comparator.comparing(SortingItemDto::getAcucares, Comparator.nullsLast(Double::compareTo));
                break;

            case "adequacao":
            default:
                // Compara usando o "peso" (1, 2, 3) do Enum
                comparator = Comparator.comparing(dto -> dto.getAdequacao().getPeso());
                break;
        }

        // Aplica a direção (ASC ou DESC)
        return sortOrder.isDescending() ? comparator.reversed() : comparator;
    }
}