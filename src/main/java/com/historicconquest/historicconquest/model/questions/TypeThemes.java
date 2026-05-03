package com.historicconquest.historicconquest.model.questions;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public enum TypeThemes {
    ENTERTAINMENT("Entertainment"),
    INFORMATICS("Informatics"),
    TOURISM("Tourism"),
    MYSTERY("Mystery"),
    NONE("None");


    private final String label ;
    TypeThemes(String label) {
        this.label  = label ;
    }

    public static TypeThemes getRandom() {
        TypeThemes[] valid = Arrays.stream(values())
                .filter(t -> t != NONE)
                .toArray(TypeThemes[]::new);

        return valid[ThreadLocalRandom.current().nextInt(valid.length)];
    }

    public static TypeThemes fromString(String text) {
        for (TypeThemes b : TypeThemes.values()) {
            if (b.label.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return NONE;
    }

    public String getLabel() {
        return label;
    }
}
