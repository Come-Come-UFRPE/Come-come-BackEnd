package com.backend.favorite.dtos;

import java.util.UUID;

public record CategoryDTO(UUID ownerId,
                          String categoryName) {
}
