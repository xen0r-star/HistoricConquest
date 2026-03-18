package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.util.MapBackgroundManager;
import com.historicconquest.historicconquest.util.TextureUtils;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class MapBackgroundDisplay {

    private final StackPane root;
    private final Pane mapViewport;

    /**
     * Décalage en pixels.
     * translateX négatif => vers la gauche
     * translateY négatif => vers le haut
     */
    private final double translateX;
    private final double translateY;

    /**
     * Petit biais de centrage "visuel" basé sur la largeur de la map.
     * 0.0 = centrage mathématique
     * négatif = un peu plus à gauche, positif = un peu plus à droite
     */
    private final double centerBiasX;

    private Group mapInterface;

    // Constructeur compatibilité (ancien appel: (root, viewport, scaleX, scaleY, translateY))
    public MapBackgroundDisplay(StackPane root, Pane mapViewport, double scaleX, double scaleY, double translateY) {
        this(root, mapViewport, scaleX, scaleY, 0.0, translateY, -0.03);
    }

    // Constructeur complet recommandé
    public MapBackgroundDisplay(StackPane root,
                                Pane mapViewport,
                                double scaleX,
                                double scaleY,
                                double translateX,
                                double translateY,
                                double centerBiasX) {
        this.root = root;
        this.mapViewport = mapViewport;
        this.translateX = translateX;
        this.translateY = translateY;
        this.centerBiasX = centerBiasX;
    }

    public void initialize() {
        // Couche de bruit (papier grain)
        ImageView noiseLayer = TextureUtils.generatePaperGrain(1920, 1080, 0.1);
        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);

        // Récupère la map (singleton)
        WorldMap worldMap = MapBackgroundManager.getBackgroundMap();

        // Groupe d'affichage
        mapInterface = new Group();
        mapInterface.getChildren().setAll(worldMap.getBlocs());

        // Ajoute au viewport + clip
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }

        // Re-layout dynamique (resize / bounds)
        InvalidationListener relayout = obs -> layoutMapContainCentered();
        root.widthProperty().addListener(relayout);
        root.heightProperty().addListener(relayout);

        if (mapViewport != null) {
            mapViewport.widthProperty().addListener(relayout);
            mapViewport.heightProperty().addListener(relayout);
        }

        mapInterface.layoutBoundsProperty().addListener(relayout);

        // Premier layout
        layoutMapContainCentered();
    }

    /**
     * Fit "contain" + centrage dans mapViewport (responsive).
     */
    private void layoutMapContainCentered() {
        if (mapViewport == null || mapInterface == null) return;

        double vw = mapViewport.getWidth();
        double vh = mapViewport.getHeight();
        if (vw <= 0 || vh <= 0) return;

        Bounds b = mapInterface.getLayoutBounds();
        double mapW = b.getWidth();
        double mapH = b.getHeight();
        if (mapW <= 0 || mapH <= 0) return;

        // scale "contain" => toute la map visible
        double scale = Math.min(vw / mapW, vh / mapH);

        mapInterface.setScaleX(scale);
        mapInterface.setScaleY(scale);

        double scaledW = mapW * scale;
        double scaledH = mapH * scale;

        // biais visuel basé sur la largeur de la map
        double biasPixels = (mapW * centerBiasX) * scale;

        // centre + offsets
        double x = (vw - scaledW) / 2.0 - b.getMinX() * scale + biasPixels + translateX;
        double y = (vh - scaledH) / 2.0 - b.getMinY() * scale + translateY;

        mapInterface.setTranslateX(x);
        mapInterface.setTranslateY(y);
    }
}