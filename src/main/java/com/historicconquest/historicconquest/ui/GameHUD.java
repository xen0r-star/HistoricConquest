package com.historicconquest.historicconquest.ui;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Random;

public class GameHUD {
    @FXML private StackPane root;
    @FXML private Pane mapViewport;

    @FXML
    public void initialize() {
        ImageView noiseLayer = new ImageView(generatePaperGrain(1920, 1080, 0.1));

        noiseLayer.setMouseTransparent(true);
        noiseLayer.setPickOnBounds(false);
        noiseLayer.setManaged(false);

        noiseLayer.setBlendMode(BlendMode.OVERLAY);

        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);
    }

    public void initializeMap(Group mapInterface) {
        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            // Map cutting
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }
    }



    public Image generatePaperGrain(int width, int height, double strength) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter writer = img.getPixelWriter();
        Random random = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = (random.nextDouble() - 0.5);
                double base = 0.5;
                double value = base + noise * strength;

                value = Math.max(0, Math.min(1, value));
                writer.setColor(x, y, Color.color(value, value, value));
            }
        }

        return img;
    }
}