package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.ComparePatchDTO;
import com.comecome.openfoodfacts.dtos.UiFilterDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ComparingResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Service
public class ComparingService {

    private final OpenFoodFactsService openFoodFactsService;

    public ComparingService(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    public Mono<ComparingResponseDTO> comparingResponse(ComparePatchDTO patchDTO){
        AnamneseSearchDTO dtoA = new AnamneseSearchDTO(patchDTO.userID(),patchDTO.idA());
        AnamneseSearchDTO dtoB = new AnamneseSearchDTO(patchDTO.userID(),patchDTO.idB());
        UiFilterDto dto = new UiFilterDto(Set.of(),Set.of(), Set.of(), Set.of());
        Mono<Map> productA = openFoodFactsService.searchProducts(dtoA,"en:brazil",dtoA.getUserID(), dto);
        Mono<Map> productB = openFoodFactsService.searchProducts(dtoB,"en:brazil",dtoB.getUserID(), dto);

        return Mono.zip(productA, productB)
                .map(tuple -> {
                    Map mapA = tuple.getT1();
                    Map mapB = tuple.getT2();

                    return new ComparingResponseDTO(mapA, mapB);
                });
    }
}
