package com.comecome.openfoodfacts.repositories;

import com.comecome.openfoodfacts.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

    @Query(value = """
        SELECT 
            code, url, created_t, created_datetime, last_modified_t, last_modified_datetime,
            product_name, generic_name, quantity, packaging, origins, manufacturing_places,
            labels, categories, categories_en, countries, countries_en, nutriments,
            nutriscore_grade, nutriscore_score, nova_group, allergens, brands,
            ingredients_text, ingredients_tags,
            image_url, image_small_url, image_ingredients_url, image_ingredients_small_url,
            image_nutrition_url, image_nutrition_small_url
        FROM produtos
        WHERE LOWER(product_name) LIKE LOWER('%' || :query || '%')
           OR LOWER(brands) LIKE LOWER('%' || :query || '%')
        ORDER BY
            CASE
                WHEN LOWER(product_name) = LOWER(:query) THEN 1
                WHEN LOWER(product_name) ILIKE :query THEN 2
                WHEN LOWER(product_name) ILIKE :query || ' %' THEN 3
                WHEN LOWER(product_name) LIKE '%' || :query || '%' THEN 4
                WHEN LOWER(brands) ILIKE :query THEN 5
                ELSE 6
            END,
            product_name
        LIMIT 20
        """, nativeQuery = true)
    List<Produto> buscarPorNomeOuMarca(@Param("query") String query);

    @Query(value = """
        SELECT * FROM produtos
        WHERE LOWER(product_name) LIKE LOWER('%' || :query || '%')
           OR LOWER(brands) LIKE LOWER('%' || :query || '%')
        ORDER BY
            CASE
                WHEN LOWER(product_name) = LOWER(:query) THEN 1
                WHEN LOWER(product_name) ILIKE :query THEN 2
                WHEN LOWER(product_name) ILIKE :query || ' %' THEN 3
                WHEN LOWER(product_name) LIKE '%' || :query || '%' THEN 4
                WHEN LOWER(brands) ILIKE :query THEN 5
                ELSE 6
            END,
            product_name
        LIMIT :size OFFSET :offset
        """, nativeQuery = true)
    List<Produto> buscarComPaginacao(
            @Param("query") String query,
            @Param("size") int size,
            @Param("offset") long offset);

    // Busca exata por código de barras
    Produto findByCode(String code);
}