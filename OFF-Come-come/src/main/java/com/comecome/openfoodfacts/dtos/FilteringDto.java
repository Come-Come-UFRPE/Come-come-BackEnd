package com.comecome.openfoodfacts.dtos;

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