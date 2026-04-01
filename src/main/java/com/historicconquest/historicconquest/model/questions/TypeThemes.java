package com.historicconquest.historicconquest.model.questions;

import java.util.concurrent.ThreadLocalRandom;

public enum TypeThemes {
    DIVERTISSMENT("Divertissment"),
    INFORMATIC("Informatic"),
    TOURISM("Tourism"),
    HISTORY("History"),
    NONE("None");


    TypeThemes(String label) { }

    public static TypeThemes getRandom() {
        TypeThemes[] valid = java.util.Arrays.stream(values())
                .filter(t -> t != NONE)
                .toArray(TypeThemes[]::new);

        return valid[ThreadLocalRandom.current().nextInt(valid.length)];
    }
}
