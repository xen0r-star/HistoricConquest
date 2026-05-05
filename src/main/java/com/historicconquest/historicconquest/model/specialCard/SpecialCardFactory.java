package com.historicconquest.historicconquest.model.specialCard;

import com.historicconquest.historicconquest.model.specialCard.bonus.*;
import com.historicconquest.historicconquest.model.specialCard.penalty.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpecialCardFactory {
    private static final List<SpecialCard> BONUS_CARDS = new ArrayList<>();
    private static final List<SpecialCard> MALUS_CARDS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        BONUS_CARDS.add(new FlashBoost());
//        BONUS_CARDS.add(new ForcedMarch());
//        BONUS_CARDS.add(new Propaganda());
//        BONUS_CARDS.add(new ShieldOfKnowledge());
//        BONUS_CARDS.add(new TacticalGenius());

        MALUS_CARDS.add(new MilitaryCoup());
        MALUS_CARDS.add(new PopularUprising());
//        MALUS_CARDS.add(new RadioJamming());
//        MALUS_CARDS.add(new Recession());
        MALUS_CARDS.add(new VulnerableZone());
    }

    public static SpecialCard drawBonus() {
        return getRandomFromList(BONUS_CARDS);
    }

    public static SpecialCard drawMalus() {
        return getRandomFromList(MALUS_CARDS);
    }

    public static SpecialCard getMalus(String name) {
        for (SpecialCard card : MALUS_CARDS) {
            if (card.getName().equals(name)) {
                return card;
            }
        }

        return null;
    }

    public static SpecialCard getBonus(String name) {
        for (SpecialCard card : BONUS_CARDS) {
            if (card.getName().equals(name)) {
                return card;
            }
        }
        return null;
    }

    private static SpecialCard getRandomFromList(List<SpecialCard> list) {
        double totalLuck = list.stream().mapToDouble(SpecialCard::getLuck).sum();
        double randomValue = totalLuck * RANDOM.nextDouble();

        double cumulativeLuck = 0;
        for (SpecialCard card : list) {
            cumulativeLuck += card.getLuck();
            if (randomValue <= cumulativeLuck) {
                return card;
            }
        }
        return list.getFirst();
    }
}
