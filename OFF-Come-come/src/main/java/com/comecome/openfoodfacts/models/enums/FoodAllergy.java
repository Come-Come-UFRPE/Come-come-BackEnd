package com.comecome.openfoodfacts.models.enums;

import java.util.Arrays;
import java.util.List;

public enum FoodAllergy {

    LACTOSE(Arrays.asList(
            "en:lactose",
            "en:milks",
            "en:whole-milks",
            "en:semi-skimmed-milks",
            "en:skimmed-milks",
            "en:whey",
            "en:butters",
            "en:casein",
            "en:caseinates",
            "en:yogurts",
            "en:cheeses",
            "en:creams",
            "en:curds"
    )),

    OVO(Arrays.asList(
            "en:eggs",
            "en:whole-eggs",
            "en:egg-powders",
            "en:egg-yolk",
            "en:egg-white",
            "en:egg-albumin",
            "en:ovalbumin",
            "en:ovomucoid"
    )),

    TRIGO(Arrays.asList(
            "en:wheat",
            "en:wheat-flours",
            "en:wheat-grains",
            "en:gluten",
            "en:barley",
            "en:malt",
            "en:rye",
            "en:semolina",
            "en:couscous",
            "en:triticale"
    )),

    FRUTOS_DO_MAR(Arrays.asList(
            "en:seafood",
            "en:shrimps",
            "en:lobsters",
            "en:crabs",
            "en:oysters",
            "en:mussels",
            "en:squids",
            "en:snails",
            "en:clams"
    )),

    AMENDOIM_E_OLEAGINOSAS(Arrays.asList(
            "en:nuts",
            "en:peanuts",
            "en:cashew-nuts",
            "en:brazil-nuts",
            "en:walnuts",
            "en:almonds",
            "en:pistachios",
            "en:hazelnuts"
    ));


    private final List<String> tags;


    FoodAllergy(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }
}
