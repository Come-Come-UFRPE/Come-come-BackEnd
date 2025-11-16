package com.comecome.openfoodfacts.dtos;

import java.util.UUID;

public record ComparePatchDTO(
    UUID userID,
    String idA,
    String idB) {}
