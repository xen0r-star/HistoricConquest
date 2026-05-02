package com.historicconquest.historicconquest.service.map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

public class MapNavigationService {

    private static final double ZOOM_FACTOR = 1.1;
    private static final double ZOOM_MIN = 0.75;
    private static final double ZOOM_MAX = 10.0;
    private static final double TRANSLATE_X_MIN = -1200;
    private static final double TRANSLATE_X_MAX = 800;
    private static final double TRANSLATE_Y_MIN = -400;
    private static final double TRANSLATE_Y_MAX = 500;
    private double mouseAnchorX, mouseAnchorY;
    private double translateAnchorX, translateAnchorY;


    public void attachNavigation(StackPane root, Group plateau) {
        // --- ZOOM ---
        root.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
            double oldScale = plateau.getScaleX();
            double newScale = oldScale * zoomFactor;

            if (newScale < ZOOM_MIN) newScale = ZOOM_MIN;
            else if (newScale > ZOOM_MAX) newScale = ZOOM_MAX;


            double mouseSceneX = event.getSceneX();
            double mouseSceneY = event.getSceneY();

            Point2D mouseInMap = plateau.sceneToLocal(mouseSceneX, mouseSceneY);

            plateau.setScaleX(newScale);
            plateau.setScaleY(newScale);

            Point2D mouseInSceneAfterZoom = plateau.localToScene(mouseInMap);

            double errorX = mouseInSceneAfterZoom.getX() - mouseSceneX;
            double errorY = mouseInSceneAfterZoom.getY() - mouseSceneY;

            plateau.setTranslateX(plateau.getTranslateX() - errorX);
            plateau.setTranslateY(plateau.getTranslateY() - errorY);

            clampTranslate(plateau);
            event.consume();
        });

        // --- MOVE ---
        root.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = plateau.getTranslateX();
            translateAnchorY = plateau.getTranslateY();
        });

        root.setOnMouseDragged(event -> {
            plateau.setTranslateX(translateAnchorX + (event.getSceneX() - mouseAnchorX));
            plateau.setTranslateY(translateAnchorY + (event.getSceneY() - mouseAnchorY));
            clampTranslate(plateau);
        });
    }

    private void clampTranslate(Group plateau) {
        double clampedX = Math.clamp(plateau.getTranslateX(), TRANSLATE_X_MIN, TRANSLATE_X_MAX);
        double clampedY = Math.clamp(plateau.getTranslateY(), TRANSLATE_Y_MIN, TRANSLATE_Y_MAX);
        plateau.setTranslateX(clampedX);
        plateau.setTranslateY(clampedY);
    }
}
