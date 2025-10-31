package com.comecome.openfoodfacts.utils;

import java.util.HashMap;
import java.util.Map;

public class AllergenMap {
    private static final Map<String, String> ALLEREGNS = new HashMap<>();

    // fonte = https://world.openfoodfacts.org/facets/ingredients/lard/allergens
    static {
        ALLEREGNS.put("gluten", "Glúten");
        ALLEREGNS.put("milk", "Leite");
        ALLEREGNS.put("soybeans", "Soja");
        ALLEREGNS.put("eggs", "Ovos");
        ALLEREGNS.put("pork", "Carne suína");
        ALLEREGNS.put("celery", "Aipo");
        ALLEREGNS.put("sulphur dioxide and sulphites", "Dióxido de enxofre e sulfitos");
        ALLEREGNS.put("mustard", "Mostarda");
        ALLEREGNS.put("nuts", "Nozes");
        ALLEREGNS.put("beef", "Carne bovina");
        ALLEREGNS.put("sesame seeds", "Sementes de gergelim");
        ALLEREGNS.put("fish", "Peixe");
        ALLEREGNS.put("chicken", "Frango");
        ALLEREGNS.put("molluscs", "Moluscos");
        ALLEREGNS.put("gelatin", "Gelatina");
        ALLEREGNS.put("peanuts", "Amendoim");
        ALLEREGNS.put("apple", "Maçã");
        ALLEREGNS.put("crustaceans", "Crustáceos");
        ALLEREGNS.put("none", "Nenhum");
        ALLEREGNS.put("lupin", "Tremoço");
        ALLEREGNS.put("fr:pistaches-pistacia-vera", "Pistache");
        ALLEREGNS.put("pt:margarina", "Margarina");
        ALLEREGNS.put("pt:vinho-madeira", "Vinho da Madeira");
        ALLEREGNS.put("fr:anhydride-sulfureux-et-sulfites-en-concentrations-de-plus-de-10-mg-kg-ou-10-mg-litre-expr-en-so2", "Anidrido sulfuroso e sulfitos");
        ALLEREGNS.put("cs:kvásek", "Fermento natural");
        ALLEREGNS.put("cs:majonéza", "Maionese");
        ALLEREGNS.put("cs:mléčné", "Derivados de leite");
        ALLEREGNS.put("cs:pšeničná-sladová-mouka", "Farinha de malte de trigo");
        ALLEREGNS.put("cs:pšeničný-kvas", "Fermento de trigo");
        ALLEREGNS.put("cs:vaječné-žloutky", "Gemas de ovo");
        ALLEREGNS.put("amande", "Amêndoa");
        ALLEREGNS.put("banana", "Banana"); //Precisa msm? ksksksks
        ALLEREGNS.put("pasteurised-free-range-egg", "Ovo caipira pasteurizado");
        ALLEREGNS.put("es:ausencia", "Ausência");
        ALLEREGNS.put("fr:derives", "Derivados");
        ALLEREGNS.put("fr:farine-de-cereales-maltees", "Farinha de cereais maltados");
        ALLEREGNS.put("fr:levain", "Fermento");
        ALLEREGNS.put("fr:metabisulfite-de-sodium", "Metabissulfito de sódio");
        ALLEREGNS.put("fr:mousse-de-canard-au-porto", "Mousse de pato ao vinho do Porto");
        ALLEREGNS.put("fr:noisettes-corylus-avellana", "Avelã");
        ALLEREGNS.put("it:anidride-solforosa-e-derivati", "Anidrido sulfuroso e derivados");
        ALLEREGNS.put("ja:え-び", "Camarão");
        ALLEREGNS.put("ja:さば･大豆･鶏肉･豚肉･ゼラチン", "Cavala, soja, frango, porco e gelatina");
        ALLEREGNS.put("ja:りんごペースト", "Pasta de maçã");
        ALLEREGNS.put("ja:パン粉", "Farinha de rosca");
        ALLEREGNS.put("ja:フライミックス粉", "Farinha para fritura");
        ALLEREGNS.put("ja:ラード", "Banha");
        ALLEREGNS.put("ja:小麦･ゼラチン･大豆･鶏肉･りんご", "Trigo, gelatina, soja, frango e maçã");
        ALLEREGNS.put("ja:牛脂", "Gordura bovina");
        ALLEREGNS.put("ja:鶏肉･豚肉･りんご", "Frango, porco e maçã");
        ALLEREGNS.put("pl:laktoza", "Lactose");
        ALLEREGNS.put("pl:sojowego", "Derivado de soja");
    }

    //* Traduz o nome do alergênico para português, se possível */
    public static String translate(String allergen) {
        if (ALLEREGNS.containsKey(allergen)) {
            return ALLEREGNS.get(allergen);
        }

        // tenta remover prefixo, tipo "fr:" ou "cs:"
        if (allergen.contains(":")) {
            String semPrefixo = allergen.substring(allergen.indexOf(":") + 1);
            if (ALLEREGNS.containsKey(semPrefixo)) {
                return ALLEREGNS.get(semPrefixo);
            }
        }

        // retorna o original se não encontrado
        return allergen;
    }
}
