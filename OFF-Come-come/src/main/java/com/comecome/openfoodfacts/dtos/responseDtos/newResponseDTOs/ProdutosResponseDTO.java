package com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs;

import java.util.List;

public record ProdutosResponseDTO(
        List<NewProductResponseDTO> produtos
) {}
