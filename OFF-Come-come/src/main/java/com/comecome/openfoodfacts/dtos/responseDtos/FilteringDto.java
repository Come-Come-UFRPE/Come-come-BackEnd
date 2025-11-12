package com.comecome.openfoodfacts.dtos.responseDtos;

import com.comecome.openfoodfacts.dtos.AnamneseSearchDTO;
import com.comecome.openfoodfacts.dtos.UiFilterDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/*
 * DTO que une:
 * - Dados da busca (query + userID)
 * - Filtros da UI (categorias, alergias, etc)
 */
public record FilteringDto(
    
    @NotNull
    @Valid
    AnamneseSearchDTO search,

    @Valid
    UiFilterDto uiFilter 

) {}