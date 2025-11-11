package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import com.comecome.openfoodfacts.dtos.responseDtos.IngredientDto;
import com.comecome.openfoodfacts.dtos.responseDtos.NutrientLevelsDto;
import com.comecome.openfoodfacts.dtos.responseDtos.ProductResponseDto;
import com.comecome.openfoodfacts.models.enums.Diet;
import com.comecome.openfoodfacts.models.enums.FoodAllergy;
import com.comecome.openfoodfacts.models.enums.HealthCondition;
import com.comecome.openfoodfacts.models.enums.Objective;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FilteringResponseService {

    public Map<String, List<ProductResponseDto>> filteringResponse(
            Map<String, List<ProductResponseDto>> rawResponse, // <-- 2. O parâmetro é Map (não Mono)
            AnamnesePatchDto anamnesePatchDto) {

        List<ProductResponseDto> produtos = rawResponse.get("products");

        List<ProductResponseDto> produtosComViolacoes = produtos.stream()
                .map(produto -> {

                    List<String> todasAsViolacoes = validateProduct(produto, anamnesePatchDto);

                    return new ProductResponseDto(
                            produto.name(),
                            produto.image(),
                            produto.details(),
                            todasAsViolacoes
                    );
                })
                .toList();

        return Map.of("products", produtosComViolacoes);
    }

    // ... (o resto da sua classe 'validateProduct', etc. continua igual)




    /*
     * Função para validar valores
     */
    private List<String> validateProduct(ProductResponseDto produto, AnamnesePatchDto anamnesePatchDto) {
        List<String> violations = new ArrayList<>();

        System.out.println(anamnesePatchDto.diet());

        // --- 1. VIOLAÇÕES DE DIETA ---
        anamnesePatchDto.diet().stream()
                .flatMap(diet -> checkDietViolation(produto, diet).stream())
                .forEach(violations::add); // Adiciona todas as violações de dieta

        // --- 2. VIOLAÇÕES DE ALERGIA ---
        anamnesePatchDto.foodAllergy().stream()
                .flatMap(allergy -> checkAllergyViolation(produto, allergy).stream())
                .forEach(violations::add); // Adiciona todas as violações de alergia

        // --- 3. VIOLAÇÕES DE CONDIÇÃO DE SAÚDE ---
        anamnesePatchDto.healthCondition().stream()
                .flatMap(condition -> checkHealthConditionViolation(produto, condition).stream())
                .forEach(violations::add); // Adiciona todas as violações de saúde

        // --- 4. VIOLAÇÕES DE OBJETIVO ---
        if (anamnesePatchDto.objective() != null) {
            violations.addAll(checkObjectiveViolation(produto, anamnesePatchDto.objective()));
        }

        return violations;
    }



    private List<String> checkDietViolation(ProductResponseDto produto, Diet diet){
        String violationTag = switch (diet) {
            case BAIXO_CARBOIDRATO -> checkLowCarb(produto) ? null : "VIOLACAO_BAIXO_CARBOIDRATO";
            case VEGETARIANA -> checkVegetarian(produto) ? null : "VIOLACAO_VEGETARIANA";
            case VEGANA -> checkVegan(produto) ? null : "VIOLACAO_VEGANA";
            case DASH -> checkDash(produto) ? null : "VIOLACAO_DASH";
            case MEDITERRANIA -> checkMediterrania(produto) ? null : "VIOLACAO_MEDITERRANIA";
            default -> null;
        };
        return violationTag != null ? List.of(violationTag) : List.of();
    }

    private List<String> checkAllergyViolation(ProductResponseDto produto, FoodAllergy allergy){
        String violationTag = switch (allergy) {
            case LACTOSE -> checkIfContainsLactose(produto) ? "VIOLACAO_LACTOSE" : null;
            case OVO -> checkIfContainsEgg(produto) ? "VIOLACAO_OVO" : null;
            case TRIGO -> checkIfContainsWheat(produto) ? "VIOLACAO_TRIGO" : null;
            case FRUTOS_DO_MAR -> checkIfContainsSeaFood(produto) ? "VIOLACAO_FRUTOS_DO_MAR" : null;
            case AMENDOIM_E_OLEAGINOSAS -> checkIfContainsNuts(produto) ? "VIOLACAO_AMENDOIM" : null;
            default -> null;
        };
        return violationTag != null ? List.of(violationTag) : List.of();
    }

    private List<String> checkHealthConditionViolation(ProductResponseDto produto, HealthCondition condition){

        String violationTag = switch (condition) {
            case HIPERTENSAO -> checkHypertension(produto) ? "VIOLACAO_HIPERTENSAO" : null;
            case DIABETES -> checkDiabetes(produto) ? "VIOLACAO_DIABETES" : null;
            case SOBREPESO -> checkOverweight(produto) ? "VIOLACAO_SOBREPESO" : null;
            case DOENCA_CARDIOVASCULAR -> checkCardiovascular(produto) ? "VIOLACAO_CARDIOVASCULAR" : null;
            case ANEMIA -> checkAnemia(produto) ? "VIOLACAO_ANEMIA" : null;
            default -> null;
        };

        return violationTag != null ? List.of(violationTag) : List.of();
    }

    private List<String> checkObjectiveViolation(ProductResponseDto produto, Objective objective){
        String violationTag = switch (objective) {
            case PERDA_CONTROLE_PESO -> checkWeightLoss(produto) ? "VIOLACAO_PERDA_CONTROLE_PESO" : null;
            case GANHO_MASSA_MUSCULAR -> checkMassGain(produto) ? "VIOLACAO_GANHO_MASSA_MUSCULAR" : null;
            case MELHORA_SAUDE_INTESTINAL -> checkGutHealth(produto) ? "VIOLACAO_MELHORA_SAUDE_INTESTINAL" : null;
            case FORTALECIMENTO_SISTEMA_IMUNOLOGICO -> checkImmuneSystem(produto) ? "VIOLACAO_FORTALECIMENTO_SISTEMA_IMUNOLOGICO" : null;
            case HABITOS_SAUDAVEIS -> checkHealthyHabits(produto) ? "VIOLACAO_HABITOS_SAUDAVEIS" : null;
            default -> null;
        };

        return violationTag != null ? List.of(violationTag) : List.of();
    }





    /*
     * Função auxiliar para identificar valores na tabela nutricional
     */
    private Double getNutrimentValue(Map<String, Object> nutriments, String key) {

        // 1. Checa se o Map existe e contém a chave
        if (nutriments == null || !nutriments.containsKey(key)) {
            return null;
        }

        Object value = nutriments.get(key);

        // 2. Se o valor já for um número (Integer, Double, Long, etc.)
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // 3. Se o valor for uma String, tenta converter
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Erro de conversão para Double na chave " + key + ": " + value);
                return null;
            }
        }

        // 4. Para qualquer outro tipo (ex: null ou objeto complexo), retorna null
        return null;
    }

    private List<String> getIngredientNames(List<IngredientDto> ingredients) {


        if (ingredients == null) {
            return List.of(); // Retorna lista vazia se não houver dados
        }

        // 2. Stream para extrair e limpar os nomes
        return ingredients.stream()
                // Filtra nulos e garante que o campo de texto original não seja nulo
                .filter(i -> i != null && i.text() != null)
                // Mapeia para o nome (texto original)
                .map(IngredientDto::text)
                // Converte para minúsculas para facilitar a comparação
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    // ** CHECAGEM DE VIOLAÇÕES **

    /*
     * Checagem de Dietas
     */
    private boolean checkLowCarb(ProductResponseDto produto){

        List<IngredientDto> ingredientsRaw = produto.details().ingredients();
        Map<String, Object> nutriments = produto.details().nutriments();

        // Converte para uma lista de nomes simples (strings)
        List<String> ingredientNames = getIngredientNames(ingredientsRaw);

        // Especificações da dieta
        final double MAX_CARBS = 5.0;
        final double MIN_PROTEIN = 10.0;

        // Retirar dados da tabela
        Double carbs = getNutrimentValue(nutriments, "carbohydrates_100g");
        Double protein = getNutrimentValue(nutriments, "proteins_100g");

        //Se contém gorduras insaturadas
        boolean containsGoodFat = ingredientNames.stream().anyMatch(name ->
                name.contains("en:oil") ||
                        name.contains("en:olive") ||
                        name.contains("en:nuts") ||
                        name.contains("en:almond")
        );

        if (!containsGoodFat){
            return false;
        }

        if (carbs == null || carbs > MAX_CARBS) {
            return false;
        }

        if (protein == null ||  protein < MIN_PROTEIN){
            return false;
        }

        return true;
    }

    private boolean checkMediterrania(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        final double MIN_FIBER = 6.0;
        Double fiber = getNutrimentValue(nutriments, "fiber_100g");

        return (fiber != null && fiber >= MIN_FIBER);
    }

    private boolean checkVegetarian(ProductResponseDto produto){
        List<String> tags = produto.details().ingredient_tags();

        return tags.contains("en:non-vegetarian");
    }

    private boolean checkVegan(ProductResponseDto produto){
        List<String> tags = produto.details().ingredient_tags();

        return tags.contains("en:non-vegan");
    }

    private boolean checkDash(ProductResponseDto produto) {

        NutrientLevelsDto nutrientLevels = produto.details().nutrient_levels();

        // Critério 1: Nível de Sódio/Sal (Principal Foco DASH)
        // Se o nível for "high" (alto), o produto viola a dieta DASH.
        if ("high".equalsIgnoreCase(nutrientLevels.salt())) {
            return false;
        }

        // Critério 2: Nível de Gordura Saturada (DASH visa reduzir gordura saturada)
        if ("high".equalsIgnoreCase(nutrientLevels.saturated_fat())) {
            return false;
        }

        // Critério 3: Baixo Teor de Açúcares (Também importante para DASH)
        if ("high".equalsIgnoreCase(nutrientLevels.sugars())) {
            return false;
        }

        return true;
    }



    /*
     *  Checagem de Alergias Alimentares
     */
    private boolean checkIfContainsLactose(ProductResponseDto produto){
        List<String> allergens = produto.details().allergens();

        //* Verifica nos alergêncios
        if (allergens != null && !allergens.isEmpty()) {
            boolean hasMilkAllergen = allergens.stream()
                    .anyMatch(allergen -> allergen != null &&
                            allergen.toLowerCase().contains("milk") || 
                            allergen.toLowerCase().contains("lactose") );
            if (hasMilkAllergen) return true;
        }

        //* Verifica nos ingredientes
        List<String> ingredientTags = produto.details().ingredient_tags();
        if (ingredientTags != null && !ingredientTags.isEmpty()) {
            boolean hasMilkTag = ingredientTags.stream()
                    .anyMatch(tag -> tag != null && (
                            tag.equals("en:milk") ||
                            tag.equals("en:lactose") ||
                            tag.equals("en:dairy") ||
                            tag.startsWith("en:milk-") ||
                            tag.startsWith("en:cheese") ||
                            tag.startsWith("en:whey") ||
                            tag.startsWith("en:casein")
                    ));
            if (hasMilkTag) return true;
        }

        return false;
    }

    private boolean checkIfContainsEgg(ProductResponseDto produto) {
        List<String> allergens = produto.details().allergens();
        if (allergens != null && !allergens.isEmpty()) {
            boolean hasEggAllergen = allergens.stream()
                    .anyMatch(allergen -> allergen != null &&
                            allergen.toLowerCase().contains("egg"));
            if (hasEggAllergen) return true;
        }

        List<String> ingredientTags = produto.details().ingredient_tags();
        if (ingredientTags != null && !ingredientTags.isEmpty()) {
            boolean hasEggTag = ingredientTags.stream()
                    .anyMatch(tag -> tag != null && (
                            tag.equals("en:egg") ||
                            tag.startsWith("en:egg-") ||
                            tag.startsWith("en:albumen")
                    ));
            if (hasEggTag) return true;
        }

        return false;
    }

    private boolean checkIfContainsWheat(ProductResponseDto produto) {
        List<String> allergens = produto.details().allergens();
        if (allergens != null && !allergens.isEmpty()) {
            boolean hasWheatAllergen = allergens.stream()
                    .anyMatch(allergen -> allergen != null &&
                            allergen.toLowerCase().contains("wheat"));
            if (hasWheatAllergen) return true;
        }

        List<String> ingredientTags = produto.details().ingredient_tags();
        if (ingredientTags != null && !ingredientTags.isEmpty()) {
            boolean hasWheatTag = ingredientTags.stream()
                    .anyMatch(tag -> tag != null && (
                            tag.equals("en:wheat") ||
                            tag.startsWith("en:wheat-") ||
                            tag.startsWith("en:gluten")
                    ));
            if (hasWheatTag) return true;
        }

        return false;
    }

    private boolean checkIfContainsSeaFood(ProductResponseDto produto) {
        List<String> allergens = produto.details().allergens();
        if (allergens != null && !allergens.isEmpty()) {
            boolean hasSeafoodAllergen = allergens.stream()
                    .anyMatch(allergen -> allergen != null &&
                            (allergen.toLowerCase().contains("fish") ||
                            allergen.toLowerCase().contains("shellfish") ||
                            allergen.toLowerCase().contains("seafood")));
            if (hasSeafoodAllergen) return true;
        }

        List<String> ingredientTags = produto.details().ingredient_tags();
        if (ingredientTags != null && !ingredientTags.isEmpty()) {
            boolean hasSeafoodTag = ingredientTags.stream()
                    .anyMatch(tag -> tag != null && (
                            tag.equals("en:fish") ||
                            tag.equals("en:shellfish") ||
                            tag.equals("en:seafood") ||
                            tag.startsWith("en:fish-") ||
                            tag.startsWith("en:crustaceans") ||
                            tag.startsWith("en:mollusks")
                    ));
            if (hasSeafoodTag) return true;
        }

        return false;
    }

    private boolean checkIfContainsNuts(ProductResponseDto produto) {
        List<String> allergens = produto.details().allergens();
        if (allergens != null && !allergens.isEmpty()) {
            boolean hasNutAllergen = allergens.stream()
                    .anyMatch(allergen -> allergen != null &&
                            (allergen.toLowerCase().contains("nut") ||
                            allergen.equalsIgnoreCase("en:nuts")));
            if (hasNutAllergen) return true;
        }

        List<String> ingredientTags = produto.details().ingredient_tags();
        if (ingredientTags != null && !ingredientTags.isEmpty()) {
            boolean hasNutTag = ingredientTags.stream()
                    .anyMatch(tag -> tag != null && (
                            tag.equals("en:nuts") ||
                            tag.startsWith("en:nut-") ||
                            tag.startsWith("en:almond") ||
                            tag.startsWith("en:hazelnut") ||
                            tag.startsWith("en:cashew") ||
                            tag.startsWith("en:walnut") ||
                            tag.startsWith("en:pecan") ||
                            tag.startsWith("en:macadamia") ||
                            tag.startsWith("en:pistachio")
                    ));
            if (hasNutTag) return true;
        }

        return false;
    }


    /*
     * Checagem de Condição de Saúde
     */
    private boolean checkHypertension(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        // Verificar o sódio no alimento
        final double MAX_SODIUM = 0.08;
        Double sodium = getNutrimentValue(nutriments, "sodium_100g");

        if (sodium > MAX_SODIUM){
            return false;
        }

        return true;
    }

    private boolean checkDiabetes(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        // Verificar os carboidratos dos alimentos
        final double MAX_CARBS = 5.0;
        Double carbs = getNutrimentValue(nutriments, "carbohydrates_100g");

        if (carbs > MAX_CARBS){
            return false;
        }

        return true;
    }

    private boolean checkOverweight(ProductResponseDto produto){
        return false;
    }

    private boolean checkCardiovascular(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();
        NutrientLevelsDto nutrientLevels = produto.details().nutrient_levels();

        //Critério 1: Verificar o sódio dos alimentos
        final double MAX_SODIUM = 0.08;
        Double sodium = getNutrimentValue(nutriments, "sodium_100g");

        if (sodium > MAX_SODIUM){
            return false;
        }

        //Critério 2: Verificar se possui alta gordura saturada
        if ("high".equalsIgnoreCase(nutrientLevels.saturated_fat())) {
            return false;
        }

        return true;
    }

    private boolean checkAnemia(ProductResponseDto produto){
        return false;
    }



    /*
     * Checagem de Objetivo
     */
    private boolean checkWeightLoss(ProductResponseDto produto){
        NutrientLevelsDto nutrientLevels = produto.details().nutrient_levels();

        //Critério 1: Verificar se possui alta gordura saturada
        if ("high".equalsIgnoreCase(nutrientLevels.saturated_fat())) {
            return false;
        }

        //Critério 2: Verificar se possui
        if ("high".equalsIgnoreCase(nutrientLevels.sugars())){
            return false;
        }

        return true;
    }

    private boolean checkMassGain(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        // Especificações da dieta
        final double MIN_PROTEIN = 10.0;

        //Carboidratos complexos
        final double MIN_CARBS = 30.0;


        // Retirar dados da tabela
        Double carbs = getNutrimentValue(nutriments, "carbohydrates_100g");
        Double protein = getNutrimentValue(nutriments, "proteins_100g");

        if (carbs == null || carbs < MIN_CARBS) {
            return false;
        }


        if (protein == null ||  protein < MIN_PROTEIN){
            return false;
        }

        return true;
    }

    private boolean checkGutHealth(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        final double MIN_FIBER = 6.0;
        Double fiber = getNutrimentValue(nutriments, "fiber_100g");

        return (fiber != null && fiber >= MIN_FIBER);

        //Depois checar os alimentos probióticos
    }

    private boolean checkImmuneSystem(ProductResponseDto produto){
        return false;
    }

    private boolean checkHealthyHabits(ProductResponseDto produto){
        Map<String, Object> nutriments = produto.details().nutriments();

        Double novaGroup = getNutrimentValue(nutriments, "nova-group"); // coloca "nova-group" aí pfv

        //Observa o Nova Group (Alimentos ultra-processados)
        if (novaGroup != null){
            return novaGroup.intValue() == 1 || novaGroup.intValue() == 2;
        }

        return false;
    }

}
