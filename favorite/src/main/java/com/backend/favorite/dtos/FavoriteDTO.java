package com.backend.favorite.dtos;

import java.util.UUID;

public record FavoriteDTO(String productId, UUID categoryId, String productName, String url) {
}
