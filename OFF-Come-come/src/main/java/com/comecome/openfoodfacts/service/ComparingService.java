package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.ComparePatchDTO;
import com.comecome.openfoodfacts.dtos.responseDtos.ComparingResponseDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ComparingService {

    private final OpenFoodFactsService openFoodFactsService;

    public ComparingService(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    public Mono<ComparingResponseDTO> comparingResponse(ComparePatchDTO patchDTO){
        AnamneseSearchDTO dtoA = new AnamneseSearchDTO(patchDTO.userID(),patchDTO.idA());
        AnamneseSearchDTO dtoB = new AnamneseSearchDTO(patchDTO.userID(),patchDTO.idB());
        Mono<Map> productA = openFoodFactsService.searchProducts(dtoA,"en:brazil",dtoA.getUserID());
        Mono<Map> productB = openFoodFactsService.searchProducts(dtoB,"en:brazil",dtoB.getUserID());

        return Mono.zip(productA, productB)
                .map(tuple -> {
                    Map mapA = tuple.getT1();
                    Map mapB = tuple.getT2();

                    return new ComparingResponseDTO(mapA, mapB);
                });
    }
}
