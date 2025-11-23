package com.backend.favorite.repositories;

import com.backend.favorite.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByCategory_CategoryId(UUID categoryId);
    void deleteByProductIdAndCategory_CategoryId(String productId, UUID categoryId);

}
