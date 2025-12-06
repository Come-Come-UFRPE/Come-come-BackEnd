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
            code, 
            url, 
            created_t, 
            created_datetime, 
            last_modified_t, 
            last_modified_datetime,
            product_name, 
            generic_name, 
            quantity, 
            packaging, 
            origins, 
            manufacturing_places,
            labels, 
            categories, 
            categories_en, 
            countries, 
            countries_en,
            countries_tags,
            nutriments,
            nutriscore_grade, 
            nutriscore_score, 
            nova_group, 
            allergens, 
            brands,
            ingredients_text, 
            ingredients_tags,
            nutrient_levels_tags,
            ingredients_analysis_tags,
            image_url, 
            image_small_url, 
            image_ingredients_url, 
            image_ingredients_small_url,
            image_nutrition_url, 
            image_nutrition_small_url
        FROM produtos
        WHERE 
            (
                LOWER(product_name) LIKE LOWER('%' || :query || '%')
                OR LOWER(brands) LIKE LOWER('%' || :query || '%')
            )
            AND (
                countries_tags ILIKE '%brazil%'
                OR countries_tags ILIKE '%brasil%'
            )
            AND (
                ingredients_text IS NOT NULL 
                AND ingredients_text <> '' 
                AND ingredients_text <> 'NaN'
            )
            AND (
                nutriments IS NOT NULL
                AND nutriments::text <> ''
                AND nutriments::text <> '{}'
                AND jsonb_typeof(nutriments) = 'object'
            )
        ORDER BY 
            nutriscore_grade ASC NULLS LAST,
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
        """,
        nativeQuery = true)

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


    /**
     * USE COM CUIDADO - pode retornar muitos resultados
     * Considere usar findByCategory() com paginação
     */
    @Query(value = """
        SELECT *
        FROM produtos
        WHERE LOWER(categories_en) LIKE LOWER(CONCAT('%', :category, '%'))
        ORDER BY last_modified_datetime DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Produto> findByCategoryLimited(@Param("category") String category);


    /**
     * Busca produtos que pertencem a QUALQUER das categorias
     *
     * @param categories Lista de categorias (ex: ["snacks", "beverages"])
     */
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
        WHERE EXISTS (
            SELECT 1
            FROM unnest(string_to_array(:categories, ',')) AS cat
            WHERE LOWER(categories_en) LIKE LOWER(CONCAT('%', cat, '%'))
        )
          AND (ingredients_text IS NOT NULL AND ingredients_text <> '' AND ingredients_text <> 'NaN')
        ORDER BY nutriscore_score ASC NULLS LAST
        LIMIT 50
        """, nativeQuery = true)
    List<Produto> findByAnyCategory(@Param("categories") String categories);
}