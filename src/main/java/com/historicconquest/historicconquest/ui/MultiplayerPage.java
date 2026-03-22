package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.MainApp;
import com.historicconquest.historicconquest.network.api.ApiService;
import com.historicconquest.historicconquest.network.model.NetworkPlayer;
import com.historicconquest.historicconquest.network.event.RoomEventListener;
import com.historicconquest.historicconquest.network.model.RoomPlayer;
import com.historicconquest.historicconquest.network.service.RoomService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MultiplayerPage {
    private static final String PLAYER_ICON = Constant.PATH + "images/person.png";
    private static final String ROBOT_ICON = Constant.PATH + "images/robots.png";
    private static final String AREA_EMPTY_STYLE = "-fx-background-color: #EEDCBE88";
    private static final String AREA_FILLED_STYLE = "-fx-background-color: #EEDCBEDC";
    private static final int MAX_PLAYERS = 4;

    private enum PanelState {
        SELECT_MODE,
        JOIN_CODE,
        JOIN_USERNAME,
        JOIN_ROOM,
        HOST_ROOM
    }


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
    @FXML private Button AddBotHost, StartGameHost;
    @FXML private Label NumberPlayerHost, CodeGameHost;
    @FXML private VBox PlayerContainerHost;

    private PanelState currentPanel = PanelState.SELECT_MODE;

    private final List<RoomPlayer> roomPlayers = new ArrayList<>();
    private RoomService roomService;



    @FXML
    public void initialize() {
        setPanel(PanelState.SELECT_MODE);

        configureSelectModeHandlers();
        configureBackHandler();

        configureJoinPanelCodeHandlers();
        configureJoinPanelUsernameHandlers();
        configureJoinPanelRoomHandlers();

        configureHostPanelHandlers();

        configureMapBackground();
    }

    private void configureSelectModeHandlers() {
        JoinPane.setOnMouseClicked(e -> {
            setPanel(PanelState.JOIN_CODE);

            for (TextField field : getTextFields()) {
                field.requestFocus();
                break;
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
                    roomService.setListener(createRoomListener());


                    roomPlayers.clear();
                    roomPlayers.add(new RoomPlayer(
                        this.roomService.getPlayerId(),
                        "Host", "#FF0000",
                        false, 0,
                        "Waiting", false
                    ));
                    refreshHostUI();
                }
            );

            setPanel(PanelState.HOST_ROOM);
        });
    }

    private void configureBackHandler() {
        BackBtn.setOnAction(e -> {
            if (currentPanel == PanelState.SELECT_MODE) {
                MainApp.getInstance().showMenu();
                return;
            }

            if (currentPanel == PanelState.JOIN_ROOM && roomService != null) {
                roomService.quitRoom();
                roomService.disconnect();
                clearPlayerAreas(PlayerContainerJoin);
            }

            if (currentPanel == PanelState.HOST_ROOM && roomService != null) {
                roomService.deleteRoom();
                roomService.disconnect();
                clearPlayerAreas(PlayerContainerHost);
            }

            setPanel(PanelState.SELECT_MODE);
        });
    }

    private void configureJoinPanelCodeHandlers() {
        JoinBtn.setOnAction(e -> ApiService.request(
            ApiService.checkRoom(getEnteredRoomCode()),
            ApiService.CheckRoomResponse.class,
            response -> {
                if (response.exists()) {
                    setPanel(PanelState.JOIN_USERNAME);
                }
            }
        ));

        List<TextField> fields = getTextFields();
        for (int i = 0; i < fields.size(); i++) {
            int index = i;
            TextField field = fields.get(i);

            field.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) {
                    return;
                }

                if (newVal.length() > 1 && newVal.length() <= fields.size()) {
                    char[] chars = newVal.toUpperCase().toCharArray();
                    for (int j = 0; j < chars.length && j < fields.size(); j++) {
                        fields.get(j).setText(String.valueOf(chars[j]));
                    }
                    fields.get(Math.min(chars.length, fields.size()) - 1).requestFocus();
                    return;
                }

                String value = newVal.substring(0, 1).toUpperCase();
                if (!value.equals(field.getText())) {
                    field.setText(value);
                }

                if (index < fields.size() - 1) {
                    fields.get(index + 1).requestFocus();
                }
            });

            field.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE && field.getText().isEmpty() && index > 0) {
                    fields.get(index - 1).requestFocus();
                }
            });
        }
    }

    private void configureJoinPanelUsernameHandlers() {
        ViewRoomBtn.setOnAction(event -> ApiService.request(
            ApiService.joinRoom(getEnteredRoomCode(), UsernameTF.getText()),
            ApiService.JoinRoomResponse.class,
            response -> {
                this.roomService = new RoomService(response.token());
                roomService.setListener(createRoomListener());

                for (NetworkPlayer player : response.players()) {
                    roomPlayers.add(new RoomPlayer(
                        player.id(), player.pseudo(), player.color(),
                        false, 0, player.status(), false
                    ));
                }

                refreshJoinUI();

                setPanel(PanelState.JOIN_ROOM);
            }
        ));
    }

    private void configureJoinPanelRoomHandlers() {
        StatusJoin.setOnAction(e -> {
            if (currentPanel == PanelState.JOIN_ROOM) {
                roomService.switchStatus();

            } else if (currentPanel == PanelState.HOST_ROOM) {
                // Check que tous les joueurs sont Ready
            }
        });
    }

    private void configureHostPanelHandlers() {
        StartGameHost.setOnAction(e -> System.out.println("Start button clicked"));

        CodeGameHost.setOnMouseClicked(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(CodeGameHost.getText().replace(" ", ""));
            Clipboard.getSystemClipboard().setContent(content);
        });

        AddBotHost.setOnAction(e -> {
            if (roomPlayers.size() < MAX_PLAYERS) {
                roomPlayers.add(new RoomPlayer(
                    "ID_BOT_"+ (roomPlayers.size()),
                    "Bot " + (roomPlayers.size()),
                    "#FF0000",
                    true, 0,
                    "Ready", true
                ));

                refreshHostUI();
            }

            roomService.addBot();
        });
    }

    private void configureMapBackground() {
        MapBackgroundDisplay mapDisplay = MapBackgroundDisplay.getInstance();
        mapDisplay.setTransformations(0.8, 0.8, 100);
        mapDisplay.display(root, mapViewport);
    }



    private RoomEventListener createRoomListener() {
        return new RoomEventListener() {
            @Override
            public void onPlayerJoin(NetworkPlayer newPlayer) {
                Platform.runLater(() -> {
                    if (currentPanel == PanelState.JOIN_ROOM) {
                        roomPlayers.add(new RoomPlayer(
                            newPlayer.id(),     newPlayer.pseudo(),
                            newPlayer.color(), !newPlayer.type().equals("player"),
                            newPlayer.ping(),   newPlayer.status(),
                            false
                        ));

                        refreshJoinUI();

                    } else if (currentPanel == PanelState.HOST_ROOM) {
                        roomPlayers.add(new RoomPlayer(
                            newPlayer.id(),     newPlayer.pseudo(),
                            newPlayer.color(), !newPlayer.type().equals("player"),
                            newPlayer.ping(),   newPlayer.status(),
                            true
                        ));

                        refreshHostUI();
                    }
                });
            }

            @Override
            public void onPlayerQuit(String playerId) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            roomPlayers.remove(player);
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                });
            }

            @Override
            public void onPlayerKick(String playerId) {
                Platform.runLater(() -> {
                    if (Objects.equals(playerId, roomService.getPlayerId())) {
                        roomService.quitRoom();
                        roomService.disconnect();

                        if (currentPanel == PanelState.JOIN_ROOM) {
                            clearPlayerAreas(PlayerContainerJoin);

                        } else if (currentPanel == PanelState.HOST_ROOM) {
                            clearPlayerAreas(PlayerContainerHost);
                        }

                        roomService.quitRoom();
                        roomService.disconnect();
                        setPanel(PanelState.SELECT_MODE);

                    } else {
                        for (RoomPlayer player : roomPlayers) {
                            if (player.getId().equals(playerId)) {
                                roomPlayers.remove(player);
                                break;
                            }
                        }

                        if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                        else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                    }
                });
            }

            @Override
            public void onPlayerColorChange(String playerId, String newColor) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            player.setColor(newColor);
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                });
            }

            @Override
            public void onPlayerPseudoChange(String playerId, String newPseudo) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            player.setName(newPseudo);
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                });
            }

            @Override
            public void onPlayerStatusChange(String playerId, String newStatus) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            player.setStatus(newStatus);
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                });
            }

            @Override
            public void onPlayerPings(Map<String, Integer> pings) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        Integer ping = pings.get(player.getId());
                        if (ping != null) player.setPing(ping);
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) refreshHostUI();
                });
            }

            @Override
            public void onRoomDeleted() {
                Platform.runLater(() -> {
                    if (currentPanel == PanelState.JOIN_ROOM) {
                        clearPlayerAreas(PlayerContainerJoin);

                    } else if (currentPanel == PanelState.HOST_ROOM) {
                        clearPlayerAreas(PlayerContainerHost);
                    }

                    roomService.quitRoom();
                    roomService.disconnect();
                    setPanel(PanelState.SELECT_MODE);
                });
            }
        };
    }




    private void renderPlayerAreas(VBox container, boolean isHost) {
        List<Pane> areas = new ArrayList<>();
        for (var node : container.getChildren()) {
            if (node instanceof Pane pane) {
                areas.add(pane);
            }
        }

        for (int i = 0; i < areas.size(); i++) {
            Pane area = areas.get(i);
            area.getChildren().clear();
            area.setStyle(AREA_EMPTY_STYLE);

            if (i < roomPlayers.size()) {
                RoomPlayer player = roomPlayers.get(i);

                addPlayerCard(
                    area,
                    player.getName(),
                    player.getStatus(),
                    player.getPing() + "ms",
                    player.isRobot(),
                    (isHost && player.isRemovable()) ? () -> {
                        roomPlayers.remove(player);
                        renderPlayerAreas(container, true);
                        updateNumberPlayer(NumberPlayerHost);
                        roomService.kickPlayer(player.getId());
                    } : null
                );
            }
        }
    }

    private void clearPlayerAreas(VBox container) {
        roomPlayers.clear();

        for (var node : container.getChildren()) {
            if (node instanceof Pane pane) {
                pane.getChildren().clear();
                pane.setStyle(AREA_EMPTY_STYLE);
            }
        }
    }

    private void refreshHostUI() {
        renderPlayerAreas(PlayerContainerHost, true);
        updateNumberPlayer(NumberPlayerHost);
    }

    private void refreshJoinUI() {
        renderPlayerAreas(PlayerContainerJoin, false);
        updateNumberPlayer(NumberPlayerJoin);
    }


    private void addPlayerCard(
        Pane area,
        String playerName,
        String status,
        String ping,
        boolean isRobot,
        Runnable onRemove
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Constant.PATH + "ui/multiplayer/PlayerInfo.fxml"));
            Pane node = loader.load();
            PlayerInfo playerInfo = loader.getController();

            if (playerInfo != null) {
                playerInfo.setPlayerName(playerName);
                playerInfo.setPlayerStatus(status);

                if (!isRobot) playerInfo.setPlayerPing(ping);
                else playerInfo.showPing(false);

                playerInfo.setPlayerIcon(isRobot ? ROBOT_ICON : PLAYER_ICON);

                if (onRemove != null) playerInfo.setOnRemove(onRemove);
                else playerInfo.removeOnRemove();
            }

            node.setLayoutX(14.0);
            node.setLayoutY(6.0);
            area.getChildren().add(node);
            area.setStyle(AREA_FILLED_STYLE);

        } catch (IOException e) {
            System.err.println("Error loading PlayerInfo FXML");
        }
    }



    private String getEnteredRoomCode() {
        StringBuilder code = new StringBuilder();
        for (TextField field : getTextFields()) {
            code.append(field.getText());
        }
        return code.toString();
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

    private void updateNumberPlayer(Label label) {
        label.setText(roomPlayers.size() + " / " + MAX_PLAYERS);
    }

    private void setPanel(PanelState panel) {
        this.currentPanel = panel;

        SelectModePanel.setVisible(panel == PanelState.SELECT_MODE);
        JoinPanel.setVisible(panel == PanelState.JOIN_CODE);
        JoinPanel2.setVisible(panel == PanelState.JOIN_USERNAME);
        JoinPanel3.setVisible(panel == PanelState.JOIN_ROOM);
        HostPanel.setVisible(panel == PanelState.HOST_ROOM);
    }
}
