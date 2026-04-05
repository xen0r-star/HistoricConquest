package com.historicconquest.historicconquest.view;

import com.historicconquest.historicconquest.app.App;
import com.historicconquest.historicconquest.app.AppPage;
import com.historicconquest.historicconquest.controller.MapBackgroundController;
import com.historicconquest.historicconquest.controller.NotificationController;
import com.historicconquest.historicconquest.service.network.ApiService;
import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;
import com.historicconquest.historicconquest.model.network.event.RoomEventListener;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.service.network.RoomService;
import com.historicconquest.historicconquest.service.network.SocketClient;
import com.historicconquest.historicconquest.view.multiplayer.PlayerInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class MultiplayerPage {
    private static final Logger logger = LoggerFactory.getLogger(MultiplayerPage.class);
    private static final String PLAYER_ICON = "/images/person.png";
    private static final String ROBOT_ICON = "/images/robots.png";
    private static final String AREA_EMPTY_STYLE = "-fx-background-color: #EEDCBE88";
    private static final String AREA_FILLED_STYLE = "-fx-background-color: #EEDCBEDC";
    private static final int MAX_PLAYERS = 4;

    private enum PanelState {
        SELECT_MODE,
        JOIN_CODE,
        JOIN_USERNAME,
        JOIN_ROOM,
        HOST_ROOM,
        ERROR_SERVICE
    }


    @FXML private StackPane root;
    @FXML private Pane mapViewport;

    @FXML private HBox SelectModePanel, JoinPanel, JoinPanel2, JoinPanel3, HostPanel, ErrorService;

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
        if (ApiService.serverIsUp() && SocketClient.serverIsUp()) {
            setPanel(PanelState.SELECT_MODE);

            configureSelectModeHandlers();

            configureJoinPanelCodeHandlers();
            configureJoinPanelUsernameHandlers();
            configureJoinPanelRoomHandlers();

            configureHostPanelHandlers();

        } else {
            setPanel(PanelState.ERROR_SERVICE);
        }


        configureBackHandler();
        MapBackgroundController.show(root, mapViewport, -55 ,-30, -0.03);
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
                    if (response.error() != null) {
                        NotificationController.show(
                            response.error().title(),
                            response.error().message(),
                            Notification.Type.ERROR
                        );
                        return;
                    }

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
            if (currentPanel == PanelState.SELECT_MODE || currentPanel == PanelState.ERROR_SERVICE) {
                App.getInstance().showPage(AppPage.HOME);
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
            ApiService.joinRoom(getEnteredRoomCode()),
            ApiService.JoinRoomResponse.class,
            response -> {
                if (response.error() != null) {
                    NotificationController.show(
                        response.error().title(),
                        response.error().message(),
                        Notification.Type.ERROR
                    );
                    return;
                }

                this.roomService = new RoomService(response.token());
                roomService.setListener(createRoomListener());

                for (NetworkPlayer player : response.players()) {
                    roomPlayers.add(new RoomPlayer(
                        player.id(),     player.pseudo(),
                        player.color(), !player.type().equals("player"),
                        player.ping(),   player.status(),
                        false
                    ));
                }

                setPanel(PanelState.JOIN_USERNAME);
            }
        ));


        List<TextField> fields = getTextFields();
        for (int i = 0; i < fields.size(); i++) {
            int index = i;
            TextField field = fields.get(i);

            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();

                if (newText.isEmpty()) return change;
                if (!newText.matches("\\d") ) return null;

                return change;
            };

            field.setTextFormatter(new TextFormatter<>(filter));
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() == 1 && index < fields.size() - 1) {
                    fields.get(index + 1).requestFocus();
                }
            });

            field.setOnKeyPressed(event -> {
                if (Objects.requireNonNull(event.getCode()) == KeyCode.BACK_SPACE) {
                    if (field.getText().isEmpty() && index > 0) {
                        fields.get(index - 1).requestFocus();
                    }
                }
            });

            field.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE) {
                    if (field.getText().isEmpty() && index > 0) {
                        fields.get(index - 1).requestFocus();
                    }
                }

                if (event.isControlDown() && event.getCode() == KeyCode.V) {
                    String paste = Clipboard.getSystemClipboard().getString().replace(" ", "");

                    if (paste.matches("\\d+")) {
                        char[] chars = paste.toCharArray();

                        for (int j = 0; j < chars.length && (index + j) < fields.size(); j++) {
                            fields.get(index + j).setText(String.valueOf(chars[j]));
                        }

                        int nextIndex = Math.min(index + chars.length, fields.size() - 1);
                        fields.get(nextIndex).requestFocus();
                    }

                    event.consume();
                }
            });

            field.setContextMenu(null);
        }
    }

    private void configureJoinPanelUsernameHandlers() {
        ViewRoomBtn.setOnAction(event -> {
            roomService.updatePseudo(UsernameTF.getText());

            refreshJoinUI();
            setPanel(PanelState.JOIN_ROOM);
        });
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
        StartGameHost.setOnAction(e -> {
            logger.debug("Start game button clicked in host panel");
        });

        CodeGameHost.setOnMouseClicked(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(CodeGameHost.getText().replace(" ", ""));
            Clipboard.getSystemClipboard().setContent(content);
        });

        AddBotHost.setOnAction(e -> {
            if (roomPlayers.size() < MAX_PLAYERS) {
                roomService.addBot();

            } else {
                NotificationController.show(
                    "Room Full",
                    "You cannot add more bots because the room is full.",
                    Notification.Type.INFORMATION
                );
            }
        });
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

                        if (roomPlayers.size() >= MAX_PLAYERS) {
                            AddBotHost.setDisable(true);
                        }
                    }
                });
            }

            @Override
            public void onPlayerQuit(String playerId) {
                Platform.runLater(() -> removePlayerAndRefreshUi(playerId));
            }

            @Override
            public void onPlayerKick(String playerId) {
                Platform.runLater(() -> {
                    if (Objects.equals(playerId, roomService.getPlayerId())) {
                        setPanel(PanelState.SELECT_MODE);

                        clearPlayerAreas(PlayerContainerJoin);
                        clearPlayerAreas(PlayerContainerHost);

                        roomService.quitRoom();
                        roomService.disconnect();

                    } else {
                        removePlayerAndRefreshUi(playerId);
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
                        AddBotHost.setDisable(false);
                    }

                    roomService.quitRoom();
                    roomService.disconnect();
                    setPanel(PanelState.SELECT_MODE);
                });
            }
        };
    }

    private void removePlayerAndRefreshUi(String playerId) {
        for (RoomPlayer player : roomPlayers) {
            if (player.getId().equals(playerId)) {
                roomPlayers.remove(player);
                break;
            }
        }

        if (currentPanel == PanelState.JOIN_ROOM) {
            refreshJoinUI();

        } else if (currentPanel == PanelState.HOST_ROOM) {
            refreshHostUI();

            if (roomPlayers.size() < MAX_PLAYERS) {
                AddBotHost.setDisable(false);
            }
        }
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
                    player.getId().equals(roomService.getPlayerId()),
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
        boolean isMe,
        boolean isRobot,
        Runnable onRemove
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/multiplayer/PlayerInfo.fxml"));
            Pane node = loader.load();
            PlayerInfo playerInfo = loader.getController();

            if (playerInfo != null) {
                playerInfo.setPlayerName(playerName);
                playerInfo.setAnnotation(isMe ? "(you)" : null);
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
        ErrorService.setVisible(panel == PanelState.ERROR_SERVICE);
    }
}
