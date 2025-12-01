package com.comecome.openfoodfacts.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Produto {

    @Id
    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "created_t")
    private Long createdT;

    @Column(name = "created_datetime")
    private Instant createdDatetime;

    @Column(name = "last_modified_t")
    private Long lastModifiedT;

    @Column(name = "last_modified_datetime")
    private Instant lastModifiedDatetime;

    @Column(name = "product_name", length = 500)
    private String productName;

    @Column(name = "generic_name", length = 500)
    private String genericName;

    @Column(name = "quantity", length = 100)
    private String quantity;

    @Column(name = "packaging", length = 500)
    private String packaging;

    @Column(name = "brands", length = 300)
    private String brands;

    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories;

    @Column(name = "categories_en", columnDefinition = "TEXT")
    private String categoriesEn;

    @Column(name = "origins", length = 500)
    private String origins;

    @Column(name = "manufacturing_places", length = 500)
    private String manufacturingPlaces;

    @Column(name = "labels", columnDefinition = "TEXT")
    private String labels;

    @Column(name = "countries", length = 500)
    private String countries;

    @Column(name = "countries_en", length = 500)
    private String countriesEn;

    @Column(name = "ingredients_text", columnDefinition = "TEXT")
    private String ingredientsText;

    @Column(name = "ingredients_tags", columnDefinition = "TEXT")
    private String ingredientsTags;

    @Column(name = "allergens", columnDefinition = "TEXT")
    private String allergens;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_small_url", length = 500)
    private String imageSmallUrl;

    @Column(name = "image_ingredients_url", length = 500)
    private String imageIngredientsUrl;

    @Column(name = "image_ingredients_small_url", length = 500)
    private String imageIngredientsSmallUrl;

    @Column(name = "image_nutrition_url", length = 500)
    private String imageNutritionUrl;

    @Column(name = "image_nutrition_small_url", length = 500)
    private String imageNutritionSmallUrl;

    @Column(name = "nutriscore_score")
    private Integer nutriscoreScore;

    @Column(name = "nutriscore_grade", length = 1)
    private String nutriscoreGrade;

    @Column(name = "nova_group")
    private Integer novaGroup;

    // Campo JSONB com todos os valores nutricionais (ex: energy-kcal_100g, sugars_100g, etc)
    @Column(name = "nutriments", columnDefinition = "JSONB")
    private String nutriments;

    // === Getters manuais (se não quiser usar Lombok) ===
    // (Com Lombok @Getter/@Setter você pode apagar todos esses)

    /*
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrands() { return brands; }
    public void setBrands(String brands) { this.brands = brands; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getNutriscoreGrade() { return nutriscoreGrade; }
    public void setNutriscoreGrade(String nutriscoreGrade) { this.nutriscoreGrade = nutriscoreGrade; }
    public String getIngredientsText() { return ingredientsText; }
    public void setIngredientsText(String ingredientsText) { this.ingredientsText = ingredientsText; }
    public String getIngredientsTags() { return ingredientsTags; }
    public void setIngredientsTags(String ingredientsTags) { this.ingredientsTags = ingredientsTags; }
    public String getNutriments() { return nutriments; }
    public void setNutriments(String nutriments) { this.nutriments = nutriments; }
    public Integer getNovaGroup() { return novaGroup; }
    public void setNovaGroup(Integer novaGroup) { this.novaGroup = novaGroup; }
    */
}