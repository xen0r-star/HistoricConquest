package com.historicconquest.historicconquest.questions;

import java.util.concurrent.ThreadLocalRandom;

public enum TypeThemes {
    DIVERTISSEMENT("Divertissement"),
    INFORMATIQUE("Informatique"),
    TOURISME("Tourisme"),
    HISTOIRE("Histoire"),
    NONE("None");


    TypeThemes(String label) { }

    public static TypeThemes getRandom() {
        TypeThemes[] valid = java.util.Arrays.stream(values())
                .filter(t -> t != NONE)
                .toArray(TypeThemes[]::new);

        return valid[ThreadLocalRandom.current().nextInt(valid.length)];
    }
}
