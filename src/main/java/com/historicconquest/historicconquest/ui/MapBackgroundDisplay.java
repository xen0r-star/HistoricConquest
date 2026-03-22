package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.util.Texture;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class MapBackgroundDisplay {
    private static MapBackgroundDisplay instance;

    private final Group mapInterface;
    private final ImageView noiseLayer;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double translateY = 0;


    private MapBackgroundDisplay() {
        // Création de la map
        WorldMap backgroundMap = new WorldMap(true, false, false, Color.web("#f2e1bf"), Color.web("#C5A682"));

        mapInterface = new Group();
        mapInterface.getChildren().addAll(backgroundMap.getBlocs());


        // Texture
        noiseLayer = Texture.generatePaperGrain(1920, 1080, 0.1);
        noiseLayer.setViewOrder(-1.0);
    }

    public static MapBackgroundDisplay getInstance() {
        if (instance == null) {
            instance = new MapBackgroundDisplay();
        }
        return instance;
    }

    public void setTransformations(double scaleX, double scaleY, double translateY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateY = translateY;
    }

    public void display(StackPane root, Pane mapViewport) {
        if (!root.getChildren().contains(noiseLayer)) {
            noiseLayer.fitWidthProperty().bind(root.widthProperty());
            noiseLayer.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(noiseLayer);
        }

        if (mapViewport != null && !mapViewport.getChildren().contains(mapInterface)) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clipForThisViewport = new Rectangle();
            clipForThisViewport.widthProperty().bind(mapViewport.widthProperty());
            clipForThisViewport.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clipForThisViewport);
        }

        mapInterface.setScaleX(scaleX);
        mapInterface.setScaleY(scaleY);
        mapInterface.setTranslateX(-(root.getPrefWidth() / 2 - mapInterface.getLayoutBounds().getWidth() / 2));
        mapInterface.setTranslateY(translateY);
    }
}