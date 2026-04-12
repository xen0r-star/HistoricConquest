package com.historicconquest.historicconquest.model.game;

import com.historicconquest.historicconquest.model.map.Zone;
import javafx.scene.Node;

import java.util.List;

public interface GameAnimationPort {
    void animatePawnMove(Node pawn, List<Zone> pathListe, Runnable onFinished);
}

