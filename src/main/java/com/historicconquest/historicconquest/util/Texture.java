package com.historicconquest.historicconquest.util;

import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Random;

public class Texture {
    public static ImageView generatePaperGrain(int width, int height, double strength) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter writer = img.getPixelWriter();
        Random random = new Random();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = (random.nextDouble() - 0.5);
                double base = 0.5;
                double value = base + noise * strength;

                value = Math.clamp(value, 0, 1);
                writer.setColor(x, y, Color.color(value, value, value));
            }
        }


        ImageView noiseLayer = new ImageView(img);
        noiseLayer.setMouseTransparent(true);
        noiseLayer.setPickOnBounds(false);
        noiseLayer.setManaged(false);

        noiseLayer.setBlendMode(BlendMode.OVERLAY);

        return noiseLayer;
    }
}
