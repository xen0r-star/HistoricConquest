package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.MainApp;
import com.historicconquest.historicconquest.map.WorldMap;
import com.historicconquest.historicconquest.util.TextureUtils;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class HomePage {
    public StackPane root;
    public Pane mapViewport;

    public Button newGameBtn;
    public Button loadGameBtn;
    public Button multiplayerBtn;

    public Button settingsBtn;
    public Button helpBtn;
    public Button exitBtn;


    private static final WorldMap worldMap = new WorldMap(
            true, false, false,
            Color.web("#f2e1bf"), Color.web("#C5A682")
    );

    @FXML
    public void initialize() {
        newGameBtn.setOnAction(e -> {
            newGameBtn.setDisable(true);
            MainApp.getInstance().showNewGame();
        });

        loadGameBtn.setOnAction(e -> {
            System.out.println("Load game mode");
        });

        multiplayerBtn.setOnAction(e -> {
            System.out.println("Multiplayer mode");
        });



        settingsBtn.setOnAction(e -> {
            System.out.println("Settings");
        });

        helpBtn.setOnAction(e -> MainApp.getInstance().showHelp(true));
        exitBtn.setOnAction(e -> MainApp.getInstance().exit());




        ImageView noiseLayer = TextureUtils.generatePaperGrain(1920, 1080, 0.1);

        noiseLayer.fitWidthProperty().bind(root.widthProperty());
        noiseLayer.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(noiseLayer);
        noiseLayer.setViewOrder(-1.0);



        Group mapInterface = new Group();
        mapInterface.getChildren().addAll(worldMap.getBlocs());

        if (mapViewport != null) {
            mapViewport.getChildren().add(mapInterface);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapViewport.widthProperty());
            clip.heightProperty().bind(mapViewport.heightProperty());
            mapViewport.setClip(clip);
        }

        mapInterface.setScaleX(0.80);
        mapInterface.setScaleY(0.80);


        mapInterface.setTranslateX(
                -(root.getPrefWidth() / 2 - mapInterface.getLayoutBounds().getWidth() / 2)
        );
        mapInterface.setTranslateY(100);
    }
}