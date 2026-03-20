package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.MainApp;
import com.historicconquest.historicconquest.network.ApiService;
import com.historicconquest.historicconquest.network.RoomEventListener;
import com.historicconquest.historicconquest.network.RoomService;
import com.historicconquest.historicconquest.ui.multiplayer.PlayerInfo;
import javafx.application.Platform;
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
import java.util.Objects;

public class MultiplayerPage {
    private static final String PLAYER_ICON = Constant.PATH + "images/person.png";
    private static final String ROBOT_ICON = Constant.PATH + "images/robots.png";

    @FXML private StackPane root;
    @FXML private Pane mapViewport;

    @FXML private HBox SelectModePanel, JoinPanel, JoinPanel2, JoinPanel3, HostPanel;

    // SelectModePanel
    @FXML private Pane JoinPane;
    @FXML private Pane HostPane;
    @FXML private Button BackBtn;

    // JoinPanel
    @FXML private HBox CodeBox;
    @FXML private Button JoinBtn;

    // JoinPanel2
    @FXML private Button ViewRoomBtn;
    @FXML private TextField UsernameTF;

    // JoinPanel3
    @FXML private Button StatusJoin;
    @FXML private Label NumberPlayerJoin;
    @FXML private VBox PlayerContainerJoin;

    // HostPanel
    @FXML private Button StartGameHost;
    @FXML private Label NumberPlayerHost, CodeGameHost;
    @FXML private VBox PlayerContainerHost;

    private static int panel = 1;

    private final List<String> players = new ArrayList<>();
    private RoomService roomService;


    @FXML
    public void initialize() {
        setPanel(1);

        // SelectModePanel
        JoinPane.setOnMouseClicked(e -> {
            setPanel(2);

            for (var node : CodeBox.getChildren()) {
                if (node instanceof TextField textField) {
                    textField.requestFocus();
                    break;
                }
            }
        });

        HostPane.setOnMouseClicked(e -> {
            ApiService.request(
                ApiService.createRoom("Host"),
                ApiService.CreateRoomResponse.class,
                response -> {
                    String code = response.roomCode().substring(0, 3) + " " + response.roomCode().substring(3, 6);
                    CodeGameHost.setText(code);

                    this.roomService = new RoomService(response.token());
                    roomService.setListener(new RoomEventListener() {
                        @Override
                        public void onPlayerJoin() {
                            Platform.runLater(() -> {
                                System.out.println("UI: Player joined");
                            });
                        }

                        @Override
                        public void onPlayerQuit() {
                            Platform.runLater(() -> {
                                System.out.println("UI: Player quit");
                            });
                        }

                        @Override
                        public void onPlayerColorChange() {

                        }

                        @Override
                        public void onPlayerPseudoChange() {

                        }

                        @Override
                        public void onPlayerPings() {

                        }

                        @Override
                        public void onRoomDeleted() {
                            Platform.runLater(() -> {
                                System.out.println("Room deleted");
                                setPanel(1);
                            });
                        }
                    });
                }
            );

            setPanel(5);
        });

        BackBtn.setOnAction(e -> {
            if (panel == 1) {
                MainApp.getInstance().showMenu();

            } else if (panel == 4) {
                roomService.sendNoData("/app/quit");
                roomService.disconnect();
                setPanel(1);

            } else if (panel == 5) {
                roomService.sendNoData("/app/delete");
                roomService.disconnect();
                setPanel(1);

            } else {
                setPanel(1);
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

            ApiService.request(
                ApiService.checkRoom(code.toString()),
                ApiService.CheckRoomResponse.class,
                response -> {
                    if (response.exists()) setPanel(3);
                }
            );
        });

        CodeGameHost.setOnMouseClicked(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(CodeGameHost.getText().replace(" ", ""));
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



        // JoinPanel2
        ViewRoomBtn.setOnAction(event -> {
            StringBuilder code = new StringBuilder();
            for (var node : CodeBox.getChildren()) {
                if (node instanceof TextField textField) {
                    code.append(textField.getText());
                }
            }

            ApiService.request(
                ApiService.joinRoom(code.toString(), UsernameTF.getText()),
                ApiService.JoinRoomResponse.class,
                response -> {
                    this.roomService = new RoomService(response.token());
                    roomService.setListener(new RoomEventListener() {
                        @Override
                        public void onPlayerJoin() {
                            Platform.runLater(() -> {
                                System.out.println("UI: Player joined");
                            });
                        }

                        @Override
                        public void onPlayerQuit() {
                            Platform.runLater(() -> {
                                System.out.println("UI: Player quit");
                            });
                        }

                        @Override
                        public void onPlayerColorChange() {

                        }

                        @Override
                        public void onPlayerPseudoChange() {

                        }

                        @Override
                        public void onPlayerPings() {

                        }

                        @Override
                        public void onRoomDeleted() {
                            Platform.runLater(() -> {
                                System.out.println("Room deleted");
                                setPanel(1);
                            });
                        }
                    });

                    setPanel(4);

                    NumberPlayerJoin.setText(response.players().size() + " / 4");

                    List<Pane> areas = getPlayerAreas(PlayerContainerJoin);
                    for(ApiService.Player player : response.players()) {
                        players.add(player.id());
                        addPlayer(
                            areas,
                            player.pseudo(),
                            "Waiting",
                            String.valueOf(player.ping()),
                            Objects.equals(player.type(), "player") ? PLAYER_ICON : ROBOT_ICON
                        );
                    }
                }
            );
        });



        // HostPanel
        StartGameHost.setOnAction(e -> {
            System.out.println("Start button clicked");
        });

        List<Pane> areas = getPlayerAreas(PlayerContainerHost);
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

    private List<Pane> getPlayerAreas(VBox container) {
        List<Pane> areas = new ArrayList<>();
        for (var node : container.getChildren()) {
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

    private void addPlayer(List<Pane> allAreas, String playerName, String status, String ping, String type) {
        for (Pane area : allAreas) {
            if (area.getChildren().isEmpty()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/multiplayer/PlayerInfo.fxml"));
                    Pane node = loader.load();
                    PlayerInfo playerInfo = loader.getController();

                    if (playerInfo != null) {
                        playerInfo.setPlayerName(playerName);
                        playerInfo.setPlayerStatus(status);
                        playerInfo.setPlayerPing(ping);
                        playerInfo.setPlayerIcon(type);
                    }

                    node.setLayoutX(14.0);
                    node.setLayoutY(6.0);
                    area.getChildren().add(node);
                    area.setStyle("-fx-background-color: #EEDCBEDC");

                } catch (IOException e) {
                    System.err.println("Error loading PlayerInfo FXML");
                }

                return;
            }
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
        NumberPlayerHost.setText(players.size() + " / 4");
    }


    private void setPanel(int panel) {
        MultiplayerPage.panel = panel;

        SelectModePanel.setVisible(panel == 1);
        JoinPanel.setVisible(panel == 2);
        JoinPanel2.setVisible(panel == 3);
        JoinPanel3.setVisible(panel == 4);
        HostPanel.setVisible(panel == 5);
    }
}