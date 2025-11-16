package com.comecome.openfoodfacts.dtos.responseDtos;

import reactor.core.publisher.Mono;

import java.util.Map;

public record ComparingResponseDTO (Map productA,
                                    Map productB
) { }
