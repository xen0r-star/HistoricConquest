package com.historicconquest.historicconquest.questions;

import java.util.concurrent.ThreadLocalRandom;

public enum TypeThemes {
    DIVERTISSEMENT("Divertissement"),
    INFORMATIQUE("Informatique"),
    TOURISME("Tourisme"),
    HISTOIRE("Histoire");


    TypeThemes(String label) { }

    public static TypeThemes getRandom() {
        return values()[ThreadLocalRandom.current().nextInt(values().length)];
    }
}
