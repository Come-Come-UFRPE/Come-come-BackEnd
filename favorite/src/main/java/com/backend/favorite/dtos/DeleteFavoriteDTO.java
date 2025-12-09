package com.backend.favorite.dtos;

import java.util.UUID;

public record DeleteFavoriteDTO(String productId, UUID categoryId) {
}
