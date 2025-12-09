package com.backend.favorite.repositories;

import com.backend.favorite.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findCategoryByOwnerId(UUID id);
    Optional<Category> findByCategoryNameAndOwnerId(String categoryName, UUID ownerId);
}
