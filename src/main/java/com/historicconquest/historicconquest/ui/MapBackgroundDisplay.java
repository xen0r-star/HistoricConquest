package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.util.MapBackgroundManager;
import com.historicconquest.historicconquest.util.TextureUtils;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class MapBackgroundDisplay {
    private final StackPane root;
    private final Pane mapViewport;
    private final double scaleX;
    private final double scaleY;
    private final double translateY;


    public MapBackgroundDisplay(StackPane root, Pane mapViewport, double scaleX, double scaleY, double translateY) {
        this.root = root;
        this.mapViewport = mapViewport;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.translateY = translateY;
    }


    public void initialize() {
        // Couche de bruit (papier grain)
        ImageView noiseLayer = TextureUtils.generatePaperGrain(1920, 1080, 0.1);
        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);

        // Récupère la map mise en cache (singleton)
        WorldMap worldMap = MapBackgroundManager.getBackgroundMap();

        // Crée le groupe d'affichage de la map
        Group mapInterface = new Group();
        mapInterface.getChildren().addAll(worldMap.getBlocs());

        // Ajoute la map au viewport avec clipping
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }

        // Applique les transformations (échelle et position)
        mapInterface.setScaleX(scaleX);
        mapInterface.setScaleY(scaleY);

        mapInterface.setTranslateX(
            -(root.getPrefWidth() / 2 - mapInterface.getLayoutBounds().getWidth() / 2)
        );
        mapInterface.setTranslateY(translateY);
    }
}

