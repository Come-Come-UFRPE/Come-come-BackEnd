package com.comecome.openfoodfacts.dtos.responseDtos;

import com.comecome.openfoodfacts.enums.AdequacaoEnum;
import com.comecome.openfoodfacts.dtos.gateway.AnamneseDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SortingItemDto {

    //1. O MAPA DE ALERGIAS
    private static final Map<String, List<String>> ALLERGY_TAG_MAP = new HashMap<>();

    static {
        ALLERGY_TAG_MAP.put("LACTOSE", Arrays.asList(
                "en:lactose", "en:milk", "en:milks", "en:whole-milks", "en:semi-skimmed-milks",
                "en:skimmed-milks", "en:whey", "en:butter", "en:casein", "en:caseinates",
                "en:yogurt", "en:cheese", "en:cream", "en:curd"
        ));
        ALLERGY_TAG_MAP.put("OVO", Arrays.asList(
                "en:egg", "en:eggs", "en:whole-eggs", "en:egg-powder", "en:egg-yolk",
                "en:egg-white", "en:egg-albumin", "en:ovalbumin", "en:ovomucoid"
        ));
        ALLERGY_TAG_MAP.put("TRIGO", Arrays.asList(
                "en:wheat", "en:wheat-flour", "en:wheat-grain", "en:gluten", "en:barley",
                "en:malt", "en:rye", "en:semolina", "en:couscous", "en:triticale"
        ));
        ALLERGY_TAG_MAP.put("FRUTOS_DO_MAR", Arrays.asList(
                "en:seafood", "en:shrimp", "en:lobster", "en:crab", "en:oyster",
                "en:mussel", "en:squid", "en:snail", "en:clam"
        ));
        ALLERGY_TAG_MAP.put("AMENDOIM_E_OLEAGINOSAS", Arrays.asList(
                "en:nut", "en:nuts", "en:peanut", "en:peanuts", "en:cashew-nut",
                "en:brazil-nut", "en:walnut", "en:almond", "en:pistachio", "en:hazelnut"
        ));
    }

    // --- 2. Campos do DTO

    private String nome;
    private AdequacaoEnum adequacao;
    private Double valorEnergetico; // energy-kcal_100g
    private Double proteinas; // proteins_100g
    private Double sodio; // sodium_100g (em g)
    private Double acucares; // sugars_100g
    private Double gorduraSaturada; // saturated-fat_100g
    private Double gorduraTotal; // fat_100g
    private Double ferro; // iron_100g (em mg)

    // Status de Dieta (precisa vir do OpenFoodFacts)
    private boolean isVegan;
    private boolean isVegetarian;

    private ProductResponseDto produtoOriginal;

    // 3. Construtor
    public SortingItemDto (ProductResponseDto produto, AnamneseDto anamnese) {
        this.produtoOriginal = produto;
        this.nome = produto.name();

        // 3.1. Extração de Nutrientes
        Map<String, Object> nutriments = produto.details().nutriments();
        this.valorEnergetico = extrairNutriente(nutriments, "energy-kcal_100g");
        this.proteinas = extrairNutriente(nutriments, "proteins_100g");
        this.sodio = extrairNutriente(nutriments, "sodium_100g");
        this.acucares = extrairNutriente(nutriments, "sugars_100g");
        this.gorduraSaturada = extrairNutriente(nutriments, "saturated-fat_100g");
        this.gorduraTotal = extrairNutriente(nutriments, "fat_100g");
        this.ferro = extrairNutriente(nutriments, "iron_100g");

        // 3.2. Extração de Alérgenos
        List<String> alergenosProduto = produto.details().allergens();


        // Verificação de vegano
        String status = produto.details().veganStatus();
        if ("en:vegan".equals(status) || "en:yes".equals(status)) {
            this.isVegan = true;
        }

        // 3.4. Cálculo da Adequação
        this.adequacao = calcularAdequacao(anamnese, alergenosProduto);
    }

    // 4. Lógica de Negócio
    private AdequacaoEnum calcularAdequacao(AnamneseDto anamnese, List<String> alergenosProduto) {

        // REGRA 1: ALERGIAS (Crítico - VERMELHO)
        Set<String> alergiasUsuario = anamnese.getFoodAllergy();
        if (alergenosProduto != null && alergiasUsuario != null && !alergiasUsuario.isEmpty()) {
            for (String alergiaNome : alergiasUsuario) {
                List<String> tagsProibidos = ALLERGY_TAG_MAP.get(alergiaNome);
                if (tagsProibidos != null && produtoContemAlergeno(alergenosProduto, tagsProibidos)) {
                    return AdequacaoEnum.VERMELHO;
                }
            }
        }

        // REGRA 2: DIETA (Crítico - VERMELHO)

        if (anamnese.temDieta("VEGANA") && !this.isVegan) {
            return AdequacaoEnum.VERMELHO;
        }
        if (anamnese.temDieta("VEGETARIANA") && !this.isVegetarian) {
            return AdequacaoEnum.VERMELHO;
        }


        // REGRA 3: CONDIÇÕES DE SAÚDE (Risco Alto/Médio)
        // Um score de risco, onde < 0 é VERDE, > 0 é AMARELO, > 10 é VERMELHO

        //TODO VERIFICAR VALORES
        int riskScore = 0;

        // Hipertensão (Sódio em g)
        if (anamnese.temCondicao("HIPERTENSAO")) {
            if (this.sodio != null && this.sodio > 1.5) riskScore += 10; // Alto Risco (VERMELHO)
            else if (this.sodio != null && this.sodio > 0.6) riskScore += 5; // Risco Médio (AMARELO)
        }

        // Diabetes (Açúcar em g por 100g)
        if (anamnese.temCondicao("DIABETES")) {
            if (this.acucares != null && this.acucares > 15.0) riskScore += 10; // Alto Risco
            else if (this.acucares != null && this.acucares > 5.0) riskScore += 5; // Risco Médio
        }

        // Doença Cardiovascular ou Sobrepeso (Gordura Saturada)
        if (anamnese.temCondicao("DOENCA_CARDIOVASCULAR") || anamnese.temCondicao("SOBREPESO")) {
            if (this.gorduraSaturada != null && this.gorduraSaturada > 5.0) riskScore += 10; // Alto Risco
            else if (this.gorduraSaturada != null && this.gorduraSaturada > 1.5) riskScore += 5; // Risco Médio
        }

        // --- REGRA 4: OBJETIVOS (Risco Médio - AMARELO) ---
        // Só adiciona risco AMARELO (5), não passa para VERMELHO

        // Perda de Peso vs Calorias
        if (anamnese.temObjetivo("PERDA_CONTROLE_PESO")) {
            if (this.valorEnergetico != null && this.valorEnergetico > 400)
                riskScore = Math.max(riskScore, 5); // Alto Kcal = Amarelo
        }

        // Ganho de Massa vs Proteína
        if (anamnese.temObjetivo("GANHO_MASSA_MUSCULAR")) {
            if (this.proteinas != null && this.proteinas < 5.0)
                riskScore = Math.max(riskScore, 5); // Baixa Proteína = Amarelo
        }

        // --- REGRA 5: ANEMIA (Lógica Inversa) ---
        // Se a pessoa tem anemia, ela precisa de ferro.
        // Um produto POBRE em ferro é "Amarelo" (não ajuda)
        if (anamnese.temCondicao("ANEMIA")) {
            // (Ex: < 0.5mg de ferro por 100g)
            if (this.ferro != null && this.ferro < 0.5) riskScore = Math.max(riskScore, 5);
        }

        // --- Resultado Final ---
        if (riskScore >= 10) return AdequacaoEnum.VERMELHO;
        if (riskScore > 0) return AdequacaoEnum.AMARELO;
        return AdequacaoEnum.VERDE; // Padrão
    }

    // 5. Métodos Helper
    private boolean produtoContemAlergeno(List<String> alergenosProduto, List<String> tagsProibidos) {
        for (String tagProduto : alergenosProduto) {
            for (String tagProibido : tagsProibidos) {
                if (tagProduto.trim().equalsIgnoreCase(tagProibido.trim())) return true;
            }
        }
        return false;
    }

    private Double extrairNutriente(Map<String, Object> nutriments, String chave) {
        if (nutriments == null || !nutriments.containsKey(chave)) return null;
        Object valor = nutriments.get(chave);
        try {
            if (valor instanceof Number) return ((Number) valor).doubleValue();
            if (valor instanceof String) return Double.parseDouble((String) valor);
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    //Getters e setters

    public String getNome() { return nome; }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public AdequacaoEnum getAdequacao() {
        return adequacao;
    }

    public void setAdequacao(AdequacaoEnum adequacao) {
        this.adequacao = adequacao;
    }

    public Double getValorEnergetico() {
        return valorEnergetico;
    }

    public void setValorEnergetico(Double valorEnergetico) {
        this.valorEnergetico = valorEnergetico;
    }

    public Double getProteinas() {
        return proteinas;
    }

    public void setProteinas(Double proteinas) {
        this.proteinas = proteinas;
    }

    public Double getSodio() {
        return sodio;
    }

    public void setSodio(Double sodio) {
        this.sodio = sodio;
    }

    public Double getAcucares() {
        return acucares;
    }

    public void setAcucares(Double acucares) {
        this.acucares = acucares;
    }

    public Double getGorduraSaturada() {
        return gorduraSaturada;
    }

    public void setGorduraSaturada(Double gorduraSaturada) {
        this.gorduraSaturada = gorduraSaturada;
    }

    public Double getGorduraTotal() {
        return gorduraTotal;
    }

    public void setGorduraTotal(Double gorduraTotal) {
        this.gorduraTotal = gorduraTotal;
    }

    public Double getFerro() {
        return ferro;
    }

    public void setFerro(Double ferro) {
        this.ferro = ferro;
    }

    public boolean isVegan() {
        return isVegan;
    }

    public void setVegan(boolean vegan) {
        isVegan = vegan;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public ProductResponseDto getProdutoOriginal() {
        return produtoOriginal;
    }

    public void setProdutoOriginal(ProductResponseDto produtoOriginal) {
        this.produtoOriginal = produtoOriginal;
    }
}