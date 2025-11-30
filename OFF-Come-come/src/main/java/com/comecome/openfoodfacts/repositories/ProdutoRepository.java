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

    // Versão com paginação (pra quando você quiser)
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

    // Top produtos saudáveis
    @Query(value = """
        SELECT * FROM produtos
        WHERE nutriscore_grade IN ('a', 'b')
          AND product_name IS NOT NULL
        ORDER BY 
            CASE WHEN nutriscore_grade = 'a' THEN 1 ELSE 2 END,
            product_name
        LIMIT 20
        """, nativeQuery = true)
    List<Produto> findMelhoresOpcoesSaudaveis();

    // Busca exata por código de barras (muito rápida com PK)
    Produto findByCode(String code);

    // Contagem total (pro health)
    @Query(value = "SELECT COUNT(*) FROM produtos", nativeQuery = true)
    Long contarTotalProdutos();
}