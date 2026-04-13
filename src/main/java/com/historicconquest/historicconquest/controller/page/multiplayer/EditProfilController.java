package com.historicconquest.historicconquest.controller.page.multiplayer;

import com.historicconquest.historicconquest.controller.game.PawnController;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import com.historicconquest.historicconquest.service.network.ApiService;
import com.historicconquest.historicconquest.service.network.RoomService;
import com.historicconquest.historicconquest.util.NameGenerator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class EditProfilController {
    @FXML private Pane root;
    @FXML private GridPane ColorContainer;

    @FXML private Label NameError, PawnError, TeamColorLabel;
    @FXML private TextField NameTextField;
    @FXML private StackPane HorseDrawnImage, ShipImage;

    @FXML private Button CloseBtn, SaveBtn, RandomName;
    @FXML private Circle Color1, Color2, Color3, Color4, Color5, Color6, Color7, Color8, Color9;

    private final HashSet<PlayerColor> usedPawnColors = new HashSet<>();
    private final Map<Circle, PlayerColor> circleColorMap = new LinkedHashMap<>();
    private PlayerColor selectedPawnColor;
    private Runnable onClose;
    private final PauseTransition nameDebounce = new PauseTransition(Duration.millis(300));
    private final Timeline roomRefreshTimeline = new Timeline(new KeyFrame(Duration.millis(700), e -> refreshUsedColors()));

    private long pseudoRequestVersion;
    private String lastCheckedPseudo = "";
    private boolean lastCheckedPseudoIsUsed;


    @FXML
    public void initialize() {
        NameError.setVisible(false);
        NameError.setManaged(false);
        PawnError.setVisible(false);
        PawnError.setManaged(false);

        RandomName.setOnAction(e -> NameTextField.setText(NameGenerator.get()));
        CloseBtn.setOnAction(e -> handleClose());
        SaveBtn.setOnAction(e -> saveProfil());

        nameDebounce.setOnFinished(e -> onNameInputStopped(NameTextField.getText().trim()));
        NameTextField.textProperty().addListener((obs, oldValue, newValue) -> nameDebounce.playFromStart());
        NameTextField.setOnAction(e -> {
            nameDebounce.stop();
            onNameInputStopped(NameTextField.getText().trim());
        });

        configurePreviewContainer(HorseDrawnImage, 130);
        configurePreviewContainer(ShipImage, 100.0);

        initColorSelection();

        roomRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        roomRefreshTimeline.play();
        refreshUsedColors();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setBackground(String background) {
        root.getStyleClass().setAll(background, "not-hover");
    }

    private void handleClose() {
        nameDebounce.stop();
        roomRefreshTimeline.stop();

        if (onClose != null) {
            onClose.run();
            return;
        }

        if (root != null) {
            Parent parent = root.getParent();
            if (parent instanceof Pane pane) {
                pane.getChildren().remove(root);
            }
        }
    }

    private void onNameInputStopped(String name) {
        if (name.isBlank()) {
            lastCheckedPseudo = "";
            lastCheckedPseudoIsUsed = false;
            hideNameError();
            return;
        }

        if (!RoomService.isInitialized()) {
            hideNameError();
            return;
        }

        String token = RoomService.getToken();
        if (token == null || token.isBlank()) {
            showNameError("Authentication token is missing.");
            return;
        }

        long requestVersion = ++pseudoRequestVersion;

        ApiService.request(
            ApiService.pseudoIsUsed(token, name),
            ApiService.PseudoIsUsedResponse.class,
            response -> {
                if (requestVersion != pseudoRequestVersion) {
                    return;
                }

                if (response.error() != null) {
                    showNameError(response.error().message());
                    lastCheckedPseudo = name;
                    lastCheckedPseudoIsUsed = true;
                    return;
                }

                boolean isUsed = Boolean.TRUE.equals(response.isUsed());
                lastCheckedPseudo = name;
                lastCheckedPseudoIsUsed = isUsed;

                if (isUsed) {
                    showNameError("This name is already taken.");
                } else {
                    hideNameError();
                }
            }
        );
    }

    private void initColorSelection() {
        if (ColorContainer == null) return;

        circleColorMap.clear();

        bindCircle(Color1, PlayerColor.RED);
        bindCircle(Color2, PlayerColor.ORANGE);
        bindCircle(Color3, PlayerColor.YELLOW);
        bindCircle(Color4, PlayerColor.LIME);
        bindCircle(Color5, PlayerColor.GREEN);
        bindCircle(Color6, PlayerColor.LIGHT_BLUE);
        bindCircle(Color7, PlayerColor.BLUE);
        bindCircle(Color8, PlayerColor.PINK);
        bindCircle(Color9, PlayerColor.PURPLE);

        circleColorMap.keySet().stream().findFirst()
                .ifPresent(firstCircle -> selectColorCircle(circleColorMap.get(firstCircle)));
    }

    private void bindCircle(Circle circle, PlayerColor color) {
        if (circle == null) return;

        circleColorMap.put(circle, color);
        circle.setStrokeWidth(0.0);
        circle.setStroke(Color.TRANSPARENT);
        circle.setCursor(Cursor.HAND);

        circle.setOnMouseEntered(e -> {
            if (circle.isDisable() || selectedPawnColor == color) return;
            circle.setStroke(Color.web("#8A8A8A"));
            circle.setStrokeWidth(1.5);
        });

        circle.setOnMouseExited(e -> {
            if (circle.isDisable() || selectedPawnColor == color) return;
            circle.setStroke(Color.TRANSPARENT);
            circle.setStrokeWidth(0.0);
        });

        circle.setOnMouseClicked(e -> {
            if (circle.isDisable()) return;
            selectColorCircle(color);
        });
    }

    private void selectColorCircle(PlayerColor color) {
        selectedPawnColor = color;
        hidePawnError();
        updateColorCircleVisualState();

        if (TeamColorLabel != null) {
            TeamColorLabel.setText(formatColorName(color) + " Team");
        }

        refreshPawnPreviews();
    }

    private void refreshPawnPreviews() {
        if (selectedPawnColor == null) return;

        setPreview(HorseDrawnImage, PawnController.createHorsePawn(selectedPawnColor, 122.0));
        setPreview(ShipImage, PawnController.createShipPawn(selectedPawnColor, 95.0));
    }

    private void setPreview(StackPane container, Group pawnPreview) {
        if (container == null) return;

        container.getChildren().clear();

        if (pawnPreview == null) return;

        Bounds b = pawnPreview.getLayoutBounds();
        pawnPreview.setTranslateX(-b.getMinX() - b.getWidth() / 2.0);
        pawnPreview.setTranslateY(-b.getMinY() - b.getHeight() / 2.0);

        pawnPreview.setMouseTransparent(true);
        Group centeredPreview = new Group(pawnPreview);

        StackPane.setAlignment(centeredPreview, Pos.CENTER);
        container.getChildren().add(centeredPreview);
    }

    private void configurePreviewContainer(StackPane container, double width) {
        container.setMinSize(width, 70.0);
        container.setPrefSize(width, 70.0);
        container.setMaxSize(width, 70.0);

        Rectangle clip = new Rectangle(width, 70.0);
        clip.widthProperty().bind(container.widthProperty());
        clip.heightProperty().bind(container.heightProperty());
        container.setClip(clip);
    }


    private void saveProfil() {
        String name = NameTextField.getText().trim();

        if (name.isEmpty()) {
            showNameError("Name is required.");
            return;
        }

        if (!name.equals(lastCheckedPseudo)) {
            onNameInputStopped(name);
            showNameError("Checking name availability...");
            return;
        }

        if (lastCheckedPseudoIsUsed) {
            showNameError("This name is already taken.");
            return;
        }

        if (usedPawnColors.contains(selectedPawnColor)) {
            showPawnError();
            return;
        }

        hideNameError();
        hidePawnError();

        RoomService.updatePseudo(name);
        if (selectedPawnColor != null) {
            RoomService.updateColor(selectedPawnColor.name());
        }

        handleClose();
    }

    private void refreshUsedColors() {
        if (!RoomService.isInitialized()) return;

        String token = RoomService.getToken();
        if (token == null || token.isBlank()) return;

        ApiService.request(
            ApiService.getUsedColors(token),
            ApiService.UsedColorsResponse.class,
            response -> {
                if (response.error() != null || response.colors() == null) {
                    return;
                }

                usedPawnColors.clear();
                for (String colorRaw : response.colors()) {
                    PlayerColor color = parsePlayerColor(colorRaw);
                    if (color != null) {
                        usedPawnColors.add(color);
                    }
                }

                updateColorCircleVisualState();

                if (selectedPawnColor != null && usedPawnColors.contains(selectedPawnColor)) {
                    autoSelectNextAvailableColor();
                }

                if (selectedPawnColor == null || usedPawnColors.contains(selectedPawnColor)) {
                    showPawnError();
                } else {
                    hidePawnError();
                }
            }
        );
    }

    private void updateColorCircleVisualState() {
        for (Map.Entry<Circle, PlayerColor> entry : circleColorMap.entrySet()) {
            Circle circle = entry.getKey();
            PlayerColor color = entry.getValue();
            boolean isUsed = usedPawnColors.contains(color);
            boolean isSelected = selectedPawnColor == color && !isUsed;

            circle.setDisable(isUsed);
            circle.setCursor(isUsed ? Cursor.DEFAULT : Cursor.HAND);
            circle.setOpacity(isUsed ? 0.35 : 1.0);

            if (isSelected) {
                circle.setStroke(Color.web("#636363"));
                circle.setStrokeWidth(3.0);
            } else if (!isUsed && circle.isHover()) {
                circle.setStroke(Color.web("#8A8A8A"));
                circle.setStrokeWidth(1.5);
            } else {
                circle.setStroke(Color.TRANSPARENT);
                circle.setStrokeWidth(0.0);
            }
        }
    }

    private PlayerColor parsePlayerColor(String rawColor) {
        if (rawColor == null || rawColor.isBlank()) return null;

        String normalized = rawColor.trim().toUpperCase(Locale.ROOT);
        try {
            return PlayerColor.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return switch (normalized) {
                case "#FF0000" -> PlayerColor.RED;
                case "#FFA500" -> PlayerColor.ORANGE;
                case "#FFFF00" -> PlayerColor.YELLOW;
                case "#00FF00" -> PlayerColor.LIME;
                case "#008000" -> PlayerColor.GREEN;
                case "#ADD8E6" -> PlayerColor.LIGHT_BLUE;
                case "#0000FF" -> PlayerColor.BLUE;
                case "#FFC0CB" -> PlayerColor.PINK;
                case "#800080" -> PlayerColor.PURPLE;
                default -> null;
            };
        }
    }


    private void autoSelectNextAvailableColor() {
        for (Circle circle : circleColorMap.keySet()) {
            if (!circle.isDisable()) {
                selectColorCircle(circleColorMap.get(circle));
                hidePawnError();
                return;
            }
        }

        selectedPawnColor = null;
    }

    private void showNameError(String message) {
        NameError.setText(message);
        NameError.setVisible(true);
        NameError.setManaged(true);
    }

    private void hideNameError() {
        NameError.setVisible(false);
        NameError.setManaged(false);
    }

    private void showPawnError() {
        PawnError.setText("This color is already taken.");
        PawnError.setVisible(true);
        PawnError.setManaged(true);
    }

    private void hidePawnError() {
        PawnError.setVisible(false);
        PawnError.setManaged(false);
    }

    private String formatColorName(PlayerColor color) {
        return switch (color) {
            case RED -> "Red";
            case ORANGE -> "Orange";
            case YELLOW -> "Yellow";
            case GREEN -> "Green";
            case LIME -> "Lime";
            case CYAN -> "Cyan";
            case BLUE -> "Blue";
            case LIGHT_BLUE -> "Light Blue";
            case PURPLE -> "Purple";
            case PINK -> "Pink";
        };
    }
}
