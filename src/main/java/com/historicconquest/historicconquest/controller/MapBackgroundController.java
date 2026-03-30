package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.util.Texture;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MapBackgroundController {
    private static WorldMap backgroundMap;
    private static ImageView noiseLayer;


    private MapBackgroundController() { }

    public static void initialize() {
        backgroundMap = new WorldMap(true, false, false, Color.web("#f2e1bf"), Color.web("#C5A682"));
        noiseLayer = Texture.generatePaperGrain(1920, 1080, 0.1);
    }



    public static void show(StackPane root, Pane mapViewport, double translateX, double translateY, double centerBiasX) {
        if (backgroundMap == null || noiseLayer == null) return;


        // Map
        Group mapInterface = new Group();
        mapInterface.getChildren().addAll(backgroundMap.getBlocs());

        // Texture
        noiseLayer.setViewOrder(-1.0);
        noiseLayer.fitWidthProperty().unbind();
        noiseLayer.fitHeightProperty().unbind();
        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());
        attachNoiseLayer(root);


        // Clip map
        if (mapViewport != null && !mapViewport.getChildren().contains(mapInterface)) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }


        // Resizing
        InvalidationListener relayout = obs -> layoutMapContainCentered(mapViewport, mapInterface, translateX, translateY, centerBiasX);
        root.widthProperty().addListener(relayout);
        root.heightProperty().addListener(relayout);

        if (mapViewport != null) {
            mapViewport.widthProperty().addListener(relayout);
            mapViewport.heightProperty().addListener(relayout);
        }

        mapInterface.layoutBoundsProperty().addListener(relayout);

        layoutMapContainCentered(mapViewport, mapInterface, translateX, translateY, centerBiasX);
    }

    private static void attachNoiseLayer(StackPane root) {
        Parent parent = noiseLayer.getParent();
        if (parent instanceof Pane pane && pane != root) {
            pane.getChildren().remove(noiseLayer);

        } else if (parent instanceof Group group) {
            group.getChildren().remove(noiseLayer);
        }

        if (!root.getChildren().contains(noiseLayer)) {
            root.getChildren().add(noiseLayer);
        }
    }


    private static void layoutMapContainCentered(Pane mapViewport, Group mapInterface, double translateX, double translateY, double centerBiasX) {
        if (mapViewport == null || mapInterface == null) return;

        double vw = mapViewport.getWidth();
        double vh = mapViewport.getHeight();
        if (vw <= 0 || vh <= 0) return;

        Bounds b = mapInterface.getLayoutBounds();
        double mapW = b.getWidth();
        double mapH = b.getHeight();
        if (mapW <= 0 || mapH <= 0) return;

        double scale = Math.min(vw / mapW, vh / mapH);

        mapInterface.setScaleX(scale);
        mapInterface.setScaleY(scale);

        double scaledW = mapW * scale;
        double scaledH = mapH * scale;

        double biasPixels = (mapW * centerBiasX) * scale;

        double x = (vw - scaledW) / 2.0 - b.getMinX() * scale + biasPixels + translateX;
        double y = (vh - scaledH) / 2.0 - b.getMinY() * scale + translateY;

        mapInterface.setTranslateX(x);
        mapInterface.setTranslateY(y);
    }
}