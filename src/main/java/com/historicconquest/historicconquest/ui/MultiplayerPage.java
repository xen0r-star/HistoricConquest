package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.MainApp;
import com.historicconquest.historicconquest.ui.multiplayer.PlayerInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiplayerPage {
    private static final String PLAYER_ICON = Constant.PATH + "images/person.png";
    private static final String ROBOT_ICON = Constant.PATH + "images/robots.png";

    @FXML private StackPane root;
    @FXML private Pane mapViewport;

    @FXML private HBox SelectModePanel, JoinPanel, HostPanel;

    // SelectModePanel
    @FXML private Pane JoinPane;
    @FXML private Pane HostPane;
    @FXML private Button BackBtn;

    // JoinPanel
    @FXML private HBox CodeBox;
    @FXML private Button JoinBtn;

    // HostPanel
    @FXML private Button StartGame;
    @FXML private Label NumberPlayer, CodeGame;
    @FXML private VBox PlayerContainer;

    private static int panel = 1;


    private final List<String> players = new ArrayList<>();


    @FXML
    public void initialize() {
        panel = 1;
        SelectModePanel.setVisible(true);
        JoinPanel.setVisible(false);
        HostPanel.setVisible(false);


        // SelectModePanel
        JoinPane.setOnMouseClicked(e -> {
            panel = 2;
            SelectModePanel.setVisible(false);
            JoinPanel.setVisible(true);
            HostPanel.setVisible(false);

            for (var node : CodeBox.getChildren()) {
                if (node instanceof TextField textField) {
                    textField.requestFocus();
                    break;
                }
            }
        });

        HostPane.setOnMouseClicked(e -> {
            panel = 3;
            SelectModePanel.setVisible(false);
            JoinPanel.setVisible(false);
            HostPanel.setVisible(true);
        });

        BackBtn.setOnAction(e -> {
            if (panel == 1) {
                MainApp.getInstance().showMenu();

            } else {
                panel = 1;
                SelectModePanel.setVisible(true);
                JoinPanel.setVisible(false);
                HostPanel.setVisible(false);
            }
        });



        // JoinPanel
        JoinBtn.setOnAction(e -> {
            StringBuilder code = new StringBuilder();
            for (var node : CodeBox.getChildren()) {
                if (node instanceof TextField textField) {
                    code.append(textField.getText());
                }
            }

            System.out.println(
                "Join button clicked " +
                code
            );
        });

        CodeGame.setOnMouseClicked(e -> {
            System.out.println("Code copied: " + CodeGame.getText());

            ClipboardContent content = new ClipboardContent();
            content.putString(CodeGame.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        // Code Box
        List<TextField> fields = getTextFields();
        for (int i = 0; i < fields.size(); i++) {
            int index = i;

            fields.get(i).textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) return;

                if (newVal.length() > 1 && newVal.length() <= fields.size()) {
                    char[] chars = newVal.toUpperCase().toCharArray();

                    for (int j = 0; j < chars.length && j < fields.size(); j++) {
                        fields.get(j).setText(String.valueOf(chars[j]));
                    }

                    fields.get(Math.min(chars.length, fields.size()) - 1).requestFocus();
                    return;
                }

                String value = newVal.substring(0,1).toUpperCase();

                if (!value.equals(fields.get(index).getText())) fields.get(index).setText(value);
                if (index < fields.size() - 1)              fields.get(index + 1).requestFocus();
            });

            fields.get(i).setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && fields.get(index).getText().isEmpty() && index > 0) {
                    fields.get(index - 1).requestFocus();
                }
            });
        }



        // HostPanel
        StartGame.setOnAction(e -> {
            System.out.println("Start button clicked");
        });

        List<Pane> areas = getPlayerAreas();
        players.add("Host");
        populatePlayerArea(areas.getFirst(), "Host", areas, false);
        updateNumberPlayer();



        // Affichage de la map décoration en arrière-plan
        MapBackgroundDisplay mapDisplay = MapBackgroundDisplay.getInstance();
        mapDisplay.setTransformations(0.8, 0.8, 100);
        mapDisplay.display(root, mapViewport);
    }



    private List<TextField> getTextFields() {
        List<TextField> fields = new ArrayList<>();
        for (var node : CodeBox.getChildren()) {
            if (node instanceof TextField textField) {
                fields.add(textField);
            }
        }
        return fields;
    }

    private List<Pane> getPlayerAreas() {
        List<Pane> areas = new ArrayList<>();
        for (var node : PlayerContainer.getChildren()) {
            if (node instanceof Pane pane) {
                areas.add(pane);
            }
        }
        return areas;
    }

    private void populatePlayerArea(Pane area, String name, List<Pane> allAreas, boolean isRemove) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/multiplayer/PlayerInfo.fxml"));
            Pane node = loader.load();
            PlayerInfo playerInfo = loader.getController();

            if (playerInfo != null) {
                boolean isHost = "Host".equals(name);

                playerInfo.setPlayerName(name);
                playerInfo.setPlayerStatus(isHost ? "WAITING" : "READY");
                playerInfo.setPlayerPing(isHost ? "0" : "--");
                playerInfo.setPlayerIcon(isHost ? PLAYER_ICON : ROBOT_ICON);

                if (isRemove) {
                    playerInfo.setOnClose(() -> {
                        area.getChildren().clear();
                        area.setStyle("-fx-background-color: #EEDCBE88");
                        players.remove(name);
                        updateNumberPlayer();
                        addRobotButton(allAreas);
                    });

                } else {
                    playerInfo.removeOnRemove();
                }
            }

            node.setLayoutX(14.0);
            node.setLayoutY(6.0);
            area.getChildren().add(node);
            area.setStyle("-fx-background-color: #EEDCBEDC");

            addRobotButton(allAreas);

        } catch (IOException e) {
            System.err.println("Error loading PlayerInfo FXML");
        }
    }

    private void addRobotButton(List<Pane> allAreas) {
        for (Pane area : allAreas) {
            if (area.getChildren().isEmpty()) {
                try {
                    FXMLLoader robotLoader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/multiplayer/AddRobot.fxml"));
                    HBox addRobotBtn = robotLoader.load();
                    addRobotBtn.setOpacity(0.8);


                    area.getChildren().add(addRobotBtn);

                    addRobotBtn.setOnMouseClicked(e -> {
                        area.getChildren().clear();
                        String robotName = "Robot " + players.size();
                        players.add(robotName);
                        populatePlayerArea(area, robotName, allAreas, true);
                        updateNumberPlayer();
                    });

                } catch (IOException e) {
                    System.err.println("Error loading AddRobot FXML");
                }
            }
        }
    }

    private void updateNumberPlayer() {
        NumberPlayer.setText(players.size() + " / 4");
    }
}