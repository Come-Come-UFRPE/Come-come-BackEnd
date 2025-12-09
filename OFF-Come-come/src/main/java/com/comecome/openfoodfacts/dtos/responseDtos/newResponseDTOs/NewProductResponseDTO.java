package com.comecome.openfoodfacts.dtos.responseDtos.newResponseDTOs;

import java.util.List;

public record NewProductResponseDTO (
    String name,
    String image,
    String code,
    NewProductDetailsDTO details,
    List<String> violations
){   
}
