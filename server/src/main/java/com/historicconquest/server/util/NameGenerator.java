package com.historicconquest.server.util;

import java.util.Random;

public class NameGenerator {
    private static final String[] ADJECTIVES = {
        "Swift", "Crazy", "Silent", "Dark", "Lucky", "Wild", "Fuzzy", "Brave"
    };

    private static final String[] NOUNS = {
        "Tiger", "Ninja", "Robot", "Dragon", "Panda", "Ghost", "Falcon", "Wizard"
    };

    private static final Random RANDOM = new Random();

    private static String randomItem(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String get() {
        String adj = randomItem(ADJECTIVES);
        String noun = randomItem(NOUNS);
        int num = RANDOM.nextInt(1000);

        return adj + noun + num;
    }
}
