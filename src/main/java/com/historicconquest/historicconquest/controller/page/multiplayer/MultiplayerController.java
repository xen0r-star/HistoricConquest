package com.historicconquest.historicconquest.controller.page.multiplayer;

import com.historicconquest.historicconquest.controller.core.AppPage;
import com.historicconquest.historicconquest.controller.core.AppController;
import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.GameNetworkService;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.controller.overlay.Notification;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.model.map.WorldMap;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.questions.TypeThemes;
import com.historicconquest.historicconquest.service.network.ApiService;
import com.historicconquest.historicconquest.model.network.model.NetworkPlayer;
import com.historicconquest.historicconquest.model.network.event.RoomEventListener;
import com.historicconquest.historicconquest.model.network.model.RoomPlayer;
import com.historicconquest.historicconquest.service.network.RoomService;
import com.historicconquest.historicconquest.service.network.SocketClient;
import com.historicconquest.historicconquest.util.MapPlayerColor;
import com.historicconquest.historicconquest.util.NameGenerator;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class MultiplayerController {
    private static final Logger logger = LoggerFactory.getLogger(MultiplayerController.class);
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
    @FXML private Button ViewRoomBtn, SkipJoinBtn;
    @FXML private TextField UsernameTF;

    // JoinPanel3
    @FXML private Button StatusJoin, EditProfilJoin;
    @FXML private Label NumberPlayerJoin;
    @FXML private VBox PlayerContainerJoin;

    // HostPanel
    @FXML private Button EditProfilHost, AddBotHost, StartGameHost;
    @FXML private Label NumberPlayerHost, CodeGameHost;
    @FXML private VBox PlayerContainerHost;
    @FXML private Label countdownLabel;


    private PanelState currentPanel = PanelState.SELECT_MODE;
    private final List<RoomPlayer> roomPlayers = new ArrayList<>();
    private Pane editProfilOverlay;
    private ZoneSelectionController zoneSelectionController;
    private boolean gameStarted;
    private Timeline countdownTimeline;
    private boolean hostCanCancelStart;
    private boolean joinStatusLocked;


    @FXML
    public void initialize() {
        if (ApiService.serverIsUp() && SocketClient.serverIsUp()) {
            setPanel(PanelState.SELECT_MODE);

            configureSelectModeHandlers();

            configureJoinPanelCodeHandlers();
            configureJoinPanelUsernameHandlers();
            configureJoinPanelRoomHandlers();

            configureHostPanelHandlers();
            configureHostStatusPolling();

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
                    RoomService.create(response.token(), this::showAlert);
                    RoomService.setListener(createRoomListener());

                    String hostPseudo = response.pseudo() != null && !response.pseudo().isBlank() ?
                            response.pseudo() :
                            NameGenerator.get();
                    String hostColor = response.color();
                    RoomService.setCurrentPseudo(hostPseudo);
                    RoomService.setCurrentColor(hostColor);


                    roomPlayers.clear();
                    roomPlayers.add(new RoomPlayer(
                        RoomService.getPlayerId(),
                        hostPseudo, hostColor,
                        false, 0,
                        "Waiting", false
                    ));
                    refreshHostUI();
                    refreshHostStartState();
                }
            );

            setPanel(PanelState.HOST_ROOM);
        });
    }

    private void configureBackHandler() {
        BackBtn.setOnAction(e -> {
            if (currentPanel == PanelState.SELECT_MODE || currentPanel == PanelState.ERROR_SERVICE) {
                AppController.getInstance().showPage(AppPage.HOME);
                return;
            }

            if (currentPanel == PanelState.JOIN_ROOM && RoomService.isInitialized()) {
                RoomService.quitRoom();
                RoomService.reset();
                clearPlayerAreas(PlayerContainerJoin);
            }

            if (currentPanel == PanelState.HOST_ROOM && RoomService.isInitialized()) {
                RoomService.deleteRoom();
                RoomService.reset();
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

                RoomService.create(response.token(), this::showAlert);
                RoomService.setListener(createRoomListener());
                RoomService.setCurrentPseudo(response.pseudo());
                RoomService.setCurrentColor(response.color());

                for (NetworkPlayer player : response.players()) {
                    roomPlayers.add(new RoomPlayer(
                        player.id(),     player.pseudo(),
                        player.color(), !player.type().equals("Player"),
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
        SkipJoinBtn.setOnAction(e -> {
            refreshJoinUI();
            setPanel(PanelState.JOIN_ROOM);
        });

        ViewRoomBtn.setOnAction(event -> {
            RoomService.updatePseudo(UsernameTF.getText());

            refreshJoinUI();
            setPanel(PanelState.JOIN_ROOM);
        });
    }

    private void configureJoinPanelRoomHandlers() {
        StatusJoin.setOnAction(e -> {
            if (currentPanel == PanelState.JOIN_ROOM && !joinStatusLocked) {
                String status = RoomService.switchStatus();
                StatusJoin.setText(Objects.equals(status, "Ready") ? "Cancel" : "Ready");
            }
        });

        EditProfilJoin.setOnAction(e -> openEditProfilOverlay(false));
    }

    private void configureHostPanelHandlers() {
        EditProfilHost.setOnAction(e -> openEditProfilOverlay(true));

        StartGameHost.setOnAction(e -> {
            if (hostCanCancelStart) {
                RoomService.cancelGameStart();
            } else {
                RoomService.startGame();
            }
        });

        CodeGameHost.setOnMouseClicked(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(CodeGameHost.getText().replace(" ", ""));
            Clipboard.getSystemClipboard().setContent(content);

            NotificationController.show(
                "Code copied",
                "The room code has been copied to the clipboard.",
                Notification.Type.SUCCESS,
                5000
            );
        });

        AddBotHost.setOnAction(e -> {
            if (roomPlayers.size() < MAX_PLAYERS) {
                RoomService.addBot();

            } else {
                NotificationController.show(
                    "Room Full",
                    "You cannot add more bots because the room is full.",
                    Notification.Type.INFORMATION
                );
            }
        });
    }

    private void configureHostStatusPolling() {
        Timeline hostStatusTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshHostStartState()));
        hostStatusTimeline.setCycleCount(Timeline.INDEFINITE);
        hostStatusTimeline.play();
    }



    private RoomEventListener createRoomListener() {
        return new RoomEventListener() {
            @Override
            public void onPlayerJoin(NetworkPlayer newPlayer) {
                Platform.runLater(() -> {
                    if (currentPanel == PanelState.JOIN_ROOM) {
                        roomPlayers.add(new RoomPlayer(
                            newPlayer.id(),     newPlayer.pseudo(),
                            newPlayer.color(), !newPlayer.type().equals("Player"),
                            newPlayer.ping(),   newPlayer.status(),
                            false
                        ));

                        refreshJoinUI();

                    } else if (currentPanel == PanelState.HOST_ROOM) {
                        roomPlayers.add(new RoomPlayer(
                            newPlayer.id(),     newPlayer.pseudo(),
                            newPlayer.color(), !newPlayer.type().equals("Player"),
                            newPlayer.ping(),   newPlayer.status(),
                            true
                        ));

                        refreshHostUI();
                        refreshHostStartState();

                        if (roomPlayers.size() >= MAX_PLAYERS) {
                            AddBotHost.setDisable(true);
                        }
                    }

                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerJoin(new RoomPlayer(
                            newPlayer.id(), newPlayer.pseudo(), newPlayer.color(), !newPlayer.type().equals("Player"), newPlayer.ping(), newPlayer.status(), false
                        ));
                    }
                });
            }

            @Override
            public void onPlayerQuit(String playerId) {
                Platform.runLater(() -> {
                    removePlayerAndRefreshUi(playerId);
                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerQuit(playerId);
                    }
                });
            }

            @Override
            public void onPlayerKick(String playerId) {
                Platform.runLater(() -> {
                    if (Objects.equals(playerId, RoomService.getPlayerId())) {
                        setPanel(PanelState.SELECT_MODE);

                        clearPlayerAreas(PlayerContainerJoin);
                        clearPlayerAreas(PlayerContainerHost);

                        if (zoneSelectionController != null) {
                            zoneSelectionController.handleGameStartCancelled("You were removed from the room.");
                            zoneSelectionController = null;
                        }

                        RoomService.quitRoom();
                        RoomService.reset();

                    } else {
                        removePlayerAndRefreshUi(playerId);
                        if (zoneSelectionController != null) {
                            zoneSelectionController.handlePlayerKick(playerId);
                        }
                    }
                });
            }

            @Override
            public void onPlayerColorChange(String playerId, String newColor) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            player.setColor(newColor);
                            if (playerId.equals(RoomService.getPlayerId())) {
                                RoomService.setCurrentColor(newColor);
                            }
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) {
                        refreshHostUI();
                        refreshHostStartState();
                    }

                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerColorChange(playerId, newColor);
                    }
                });
            }

            @Override
            public void onPlayerPseudoChange(String playerId, String newPseudo) {
                Platform.runLater(() -> {
                    for (RoomPlayer player : roomPlayers) {
                        if (player.getId().equals(playerId)) {
                            player.setName(newPseudo);
                            if (playerId.equals(RoomService.getPlayerId())) {
                                RoomService.setCurrentPseudo(newPseudo);
                            }
                            break;
                        }
                    }

                    if (currentPanel == PanelState.JOIN_ROOM)      refreshJoinUI();
                    else if (currentPanel == PanelState.HOST_ROOM) {
                        refreshHostUI();
                        refreshHostStartState();
                    }

                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerPseudoChange(playerId, newPseudo);
                    }
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
                    else if (currentPanel == PanelState.HOST_ROOM) {
                        refreshHostUI();
                        refreshHostStartState();
                    }

                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerStatusChange(playerId, newStatus);
                    }
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
                    else if (currentPanel == PanelState.HOST_ROOM) {
                        refreshHostUI();
                        refreshHostStartState();
                    }

                    if (zoneSelectionController != null) {
                        zoneSelectionController.handlePlayerPings(pings);
                    }
                });
            }

            @Override
            public void onGameCountdownStarted(int seconds, long startAt) {
                Platform.runLater(() -> {
                    joinStatusLocked = true;
                    updateJoinStatusButton();
                    hostCanCancelStart = true;
                    updateHostStartButton(true, false, false);
                    syncHostCountdownStatus("Ready");
                    showCountdownOverlay(seconds);
                });
            }

            @Override
            public void onZoneSelectionStarted(int seconds, long startAt, Map<String, String> selectedZones) {
                Platform.runLater(() -> {
                    joinStatusLocked = true;
                    updateJoinStatusButton();
                    showZoneSelectionOverlay(seconds, startAt, selectedZones);
                });
            }

            @Override
            public void onZoneSelectionUpdated(Map<String, String> selectedZones) {
                Platform.runLater(() -> {
                    if (zoneSelectionController != null) {
                        zoneSelectionController.handleZoneSelectionUpdated(selectedZones);
                    }
                });
            }

            @Override
            public void onGameStartCancelled(String reason) {
                Platform.runLater(() -> {
                    joinStatusLocked = false;
                    updateJoinStatusButton();
                    hostCanCancelStart = false;
                    updateHostStartButton(false, false, false);

                    if (gameStarted) {
                        return;
                    }

                    syncHostCountdownStatus("Waiting");
                    hideCountdownOverlay();
                    boolean handledBySelection = zoneSelectionController != null;
                    if (zoneSelectionController != null) {
                        zoneSelectionController.handleGameStartCancelled(reason);
                        zoneSelectionController = null;
                    }

                    if (!handledBySelection) {
                        NotificationController.show(
                            "Start cancelled",
                            reason == null || reason.isBlank() ? "The game could not start." : reason,
                            Notification.Type.INFORMATION
                        );
                    }

                    refreshHostStartState();
                });
            }

            @Override
            public void onGameStarted(Map<String, String> selectedZones, List<String> turnOrder, String currentPlayerId, Map<String, String> listThemeZone) {
                Platform.runLater(() -> {
                    gameStarted = true;
                    joinStatusLocked = true;
                    updateJoinStatusButton();
                    hostCanCancelStart = false;
                    updateHostStartButton(false, false, false);
                    hideCountdownOverlay();
                    GameNetworkService.setStartInfo(turnOrder, currentPlayerId);
                    reorderRoomPlayers(turnOrder);

                    zoneSelectionController.applyTurnOrder(turnOrder);
                    zoneSelectionController.handleGameStarted(selectedZones);
                    zoneSelectionController = null;


                    WorldMap worldMap = MultiplayerGameOverlay.getWorldMap();
                    if (worldMap != null && listThemeZone != null) {
                        listThemeZone.forEach((zoneName, themeLabel) -> {
                            for (Zone zone : worldMap.getAllZones()) {
                                if (zone.getName().equals(zoneName)) {
                                    try {
                                        zone.setThemes(TypeThemes.fromString(themeLabel));

                                    } catch (IllegalArgumentException e) {
                                        zone.setThemes(TypeThemes.NONE);
                                    }
                                    break;
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onGameAction(String action, String playerId, String zoneName, Integer difficulty, Boolean correct) {
                Platform.runLater(() -> GameNetworkService.handleGameAction(action, playerId, zoneName, difficulty, correct));
            }

            @Override
            public void onActionSelected(String action, String playerId, String zoneName, Integer difficulty) {
                Platform.runLater(() -> GameNetworkService.handleActionSelected(action, playerId, zoneName, difficulty));
            }

            @Override
            public void onTurnChanged(String currentPlayerId, Integer currentPlayerIndex) {
                Platform.runLater(() -> GameNetworkService.handleTurnChanged(currentPlayerId, currentPlayerIndex));
            }

            @Override
            public void onAnswerResult(String playerId, Boolean correct, Integer difficulty) {
                Platform.runLater(() -> {
                    GameNetworkService.handleAnswerResult(playerId, correct);
                    GameController.getInstance().applyQuestionResult(difficulty, correct);

                    RoomPlayer roomPlayer = roomPlayers.stream()
                            .filter(obj -> Objects.equals(obj.getId(), playerId))
                            .findFirst()
                            .orElse(null);

                    String pseudo = roomPlayer != null ? roomPlayer.getName() : "Player";


                    String message = correct ? " answered the question correctly" : " failed to answer the question";

                    NotificationController.show(
                        "Quiz results",
                        pseudo + message,
                        correct ? Notification.Type.SUCCESS : Notification.Type.ERROR,
                        3000
                    );
                });
            }

            @Override
            public void onGameWon(String winnerName) {
                Platform.runLater(() -> GameNetworkService.handleGameWon(winnerName));
            }

            @Override
            public void onCoalitionRequested(String requesterId, String targetId) {
                Platform.runLater(() -> GameNetworkService.handleCoalitionRequested(requesterId, targetId));
            }

            @Override
            public void onCoalitionAccepted(String playerAId, String playerBId, String allianceColor) {
                Platform.runLater(() -> GameNetworkService.handleCoalitionAccepted(playerAId, playerBId, allianceColor));
            }

            @Override
            public void onCoalitionDeclined(String requesterId, String targetId) {
                Platform.runLater(() -> GameNetworkService.handleCoalitionDeclined(requesterId, targetId));
            }

            @Override
            public void onCoalitionBroken(String playerAId, String playerBId) {
                Platform.runLater(() -> GameNetworkService.handleCoalitionBroken(playerAId, playerBId));
            }

            @Override
            public void onRoomDeleted() {
                Platform.runLater(() -> {
                    if (gameStarted) {
                        return;
                    }

                    GameNetworkService.detach();
                    joinStatusLocked = false;
                    updateJoinStatusButton();
                    hostCanCancelStart = false;
                    updateHostStartButton(false, false, false);
                    hideCountdownOverlay();
                    if (zoneSelectionController != null) {
                        zoneSelectionController.handleRoomDeleted();
                        zoneSelectionController = null;
                        return;
                    }

                    if (currentPanel == PanelState.JOIN_ROOM) {
                        clearPlayerAreas(PlayerContainerJoin);

                    } else if (currentPanel == PanelState.HOST_ROOM) {
                        clearPlayerAreas(PlayerContainerHost);
                        AddBotHost.setDisable(false);
                    }

                    RoomService.quitRoom();
                    RoomService.reset();
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
            refreshHostStartState();

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
                    MapPlayerColor.color(player.getColor()),
                    player.getPing() + "ms",
                    player.getId().equals(RoomService.getPlayerId()),
                    player.isRobot(),
                    (isHost && player.isRemovable()) ? () -> {
                        roomPlayers.remove(player);
                        renderPlayerAreas(container, true);
                        updateNumberPlayer(NumberPlayerHost);
                        RoomService.kickPlayer(player.getId());
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

    private void refreshHostStartState() {
        if (currentPanel != PanelState.HOST_ROOM || !RoomService.isInitialized()) {
            StartGameHost.setDisable(true);
            StartGameHost.setText("Start Game");
            hostCanCancelStart = false;
            return;
        }

        String token = RoomService.getToken();
        if (token == null || token.isBlank()) {
            StartGameHost.setDisable(true);
            StartGameHost.setText("Start Game");
            hostCanCancelStart = false;
            return;
        }

        ApiService.request(
            ApiService.getGameStartStatus(token),
            ApiService.GameStartStatusResponse.class,
            response -> {
                if (response.error() != null) {
                    StartGameHost.setDisable(true);
                    StartGameHost.setText("Start Game");
                    hostCanCancelStart = false;
                    return;
                }

                boolean canStart = Boolean.TRUE.equals(response.canStart());
                boolean isStarting = Boolean.TRUE.equals(response.isStarting());
                boolean isSelecting = Boolean.TRUE.equals(response.isSelecting());
                updateHostStartButton(isStarting, canStart, isSelecting);
            }
        );
    }

    private void updateHostStartButton(boolean isStarting, boolean canStart, boolean isSelecting) {
        hostCanCancelStart = isStarting;
        StartGameHost.setText(isStarting ? "Cancel Start" : "Start Game");
        StartGameHost.setDisable(isSelecting || (!isStarting && !canStart));
    }

    private void showZoneSelectionOverlay(int seconds, long startAt, Map<String, String> selectedZones) {
        try {
            if (zoneSelectionController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/multiplayer/ZoneSelection.fxml"));
                loader.load();
                zoneSelectionController = loader.getController();

                if (zoneSelectionController == null) {
                    return;
                }

                zoneSelectionController.attach(root, new ArrayList<>(roomPlayers));
            }

            zoneSelectionController.handleZoneSelectionStarted(seconds, startAt, selectedZones);

        } catch (IOException e) {
            logger.error("Unable to load ZoneSelection overlay", e);
            NotificationController.show(
                "Display error",
                "Unable to open zone selection panel.",
                Notification.Type.ERROR
            );
        }
    }

    private void showCountdownOverlay(int seconds) {
        if (countdownLabel == null) return;

        countdownLabel.setText(String.valueOf(Math.max(seconds, 0)));
        countdownLabel.setVisible(true);

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int current = parseCountdownValue();
            if (current <= 0) {
                countdownTimeline.stop();
                updateCountdownLabel(0);
                return;
            }

            updateCountdownLabel(current - 1);
        }));
        countdownTimeline.setCycleCount(Math.max(seconds, 1));
        countdownTimeline.playFromStart();
    }

    private int parseCountdownValue() {
        if (countdownLabel == null) return 0;

        try {
            return Integer.parseInt(countdownLabel.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateCountdownLabel(int seconds) {
        if (countdownLabel != null) {
            countdownLabel.setText(String.valueOf(Math.max(seconds, 0)));
        }
    }

    private void hideCountdownOverlay() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }

        if (countdownLabel != null) {
            countdownLabel.setText("");
            countdownLabel.setVisible(false);
        }
    }

    private void syncHostCountdownStatus(String targetStatus) {
        if (currentPanel != PanelState.HOST_ROOM || !RoomService.isInitialized()) {
            return;
        }

        RoomPlayer currentPlayer = findPlayer(RoomService.getPlayerId());
        if (currentPlayer == null || Objects.equals(currentPlayer.getStatus(), targetStatus)) {
            return;
        }

        RoomService.setStatus(targetStatus);
        currentPlayer.setStatus(targetStatus);
        refreshHostUI();
    }

    private RoomPlayer findPlayer(String playerId) {
        if (playerId == null) return null;

        for (RoomPlayer player : roomPlayers) {
            if (playerId.equals(player.getId())) {
                return player;
            }
        }

        return null;
    }

    private List<Player> toGamePlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < roomPlayers.size(); i++) {
            RoomPlayer roomPlayer = roomPlayers.get(i);
            PlayerColor color = parsePlayerColor(roomPlayer.getColor());
            if (color == null) continue;

            players.add(new Player(i, roomPlayer.getName(), color));
        }
        return players;
    }

    private PlayerColor parsePlayerColor(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) return null;

        try {
            return PlayerColor.valueOf(rawColor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void refreshJoinUI() {
        renderPlayerAreas(PlayerContainerJoin, false);
        updateNumberPlayer(NumberPlayerJoin);
    }


    private void addPlayerCard(
        Pane area,
        String playerName,
        String status,
        Color color,
        String ping,
        boolean isMe,
        boolean isRobot,
        Runnable onRemove
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/multiplayer/PlayerInfo.fxml"));
            Pane node = loader.load();
            PlayerInfoController playerInfoController = loader.getController();

            if (playerInfoController != null) {
                playerInfoController.setPlayerName(playerName);
                playerInfoController.setAnnotation(isMe ? "(you)" : null);
                playerInfoController.setPlayerStatus(status);

                if (!isRobot) playerInfoController.setPlayerPing(ping);
                else playerInfoController.showPing(false);

                playerInfoController.setPlayerIcon(
                    isRobot ?
                        PlayerInfoController.Icon.ROBOT :
                        PlayerInfoController.Icon.PERSON,
                    color
                );

                if (onRemove != null) playerInfoController.setOnRemove(onRemove);
                else playerInfoController.removeOnRemove();
            }

            node.setLayoutX(14.0);
            node.setLayoutY(6.0);
            area.getChildren().add(node);
            area.setStyle(AREA_FILLED_STYLE);

        } catch (IOException e) {
            System.err.println("Error loading PlayerInfo FXML");
        }
    }

    private void openEditProfilOverlay(boolean isHost) {
        if (editProfilOverlay != null) {
            editProfilOverlay.toFront();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/multiplayer/EditProfil.fxml"));
            Pane overlay = loader.load();
            EditProfilController controller = loader.getController();

            if (controller != null) {
                controller.setOnClose(this::closeEditProfilOverlay);
                controller.setBackground(isHost ? "button-green" : "button-blue");
                controller.setInitialProfile(RoomService.getCurrentPseudo(), RoomService.getCurrentColor());
            }

            Pane backdrop = new Pane();
            backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.33);");
            backdrop.setPickOnBounds(true);
            backdrop.setOnMouseClicked(Event::consume);

            StackPane modalLayer = new StackPane(backdrop, overlay);
            modalLayer.setPickOnBounds(true);
            modalLayer.prefWidthProperty().bind(root.widthProperty());
            modalLayer.prefHeightProperty().bind(root.heightProperty());

            StackPane.setAlignment(overlay, Pos.TOP_CENTER);
            StackPane.setMargin(overlay, new Insets(250, 0, 0, 0));

            editProfilOverlay = modalLayer;
            root.getChildren().add(modalLayer);
            modalLayer.toFront();

        } catch (IOException e) {
            logger.error("Unable to load EditProfil overlay", e);
            NotificationController.show(
                "Display error",
                "Unable to open edit profile panel.",
                Notification.Type.ERROR
            );
        }
    }

    private void closeEditProfilOverlay() {
        if (editProfilOverlay == null) return;

        root.getChildren().remove(editProfilOverlay);
        editProfilOverlay = null;
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

    private void reorderRoomPlayers(List<String> turnOrder) {
        if (turnOrder == null || turnOrder.isEmpty()) {
            return;
        }

        List<RoomPlayer> ordered = new ArrayList<>();
        for (String playerId : turnOrder) {
            for (RoomPlayer player : roomPlayers) {
                if (playerId.equals(player.getId())) {
                    ordered.add(player);
                    break;
                }
            }
        }

        for (RoomPlayer player : roomPlayers) {
            if (!ordered.contains(player)) {
                ordered.add(player);
            }
        }

        roomPlayers.clear();
        roomPlayers.addAll(ordered);
    }

    private void setPanel(PanelState panel) {
        this.currentPanel = panel;

        SelectModePanel.setVisible(panel == PanelState.SELECT_MODE);
        JoinPanel.setVisible(panel == PanelState.JOIN_CODE);
        JoinPanel2.setVisible(panel == PanelState.JOIN_USERNAME);
        JoinPanel3.setVisible(panel == PanelState.JOIN_ROOM);
        HostPanel.setVisible(panel == PanelState.HOST_ROOM);
        ErrorService.setVisible(panel == PanelState.ERROR_SERVICE);

        if (panel == PanelState.JOIN_ROOM) {
            updateJoinStatusButton();
        }

        if (panel != PanelState.JOIN_ROOM && panel != PanelState.HOST_ROOM) {
            hideCountdownOverlay();
        }
    }

    private void updateJoinStatusButton() {
        if (StatusJoin == null) {
            return;
        }

        StatusJoin.setDisable(joinStatusLocked);
    }

    private void showAlert(String title, String message) {
        NotificationController.show(title, message, Notification.Type.ALERT);
    }
}

