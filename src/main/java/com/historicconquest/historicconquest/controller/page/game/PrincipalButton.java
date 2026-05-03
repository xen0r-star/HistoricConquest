package com.historicconquest.historicconquest.controller.page.game;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.MultiplayerGameOverlay;
import com.historicconquest.historicconquest.controller.page.QuestionController;
import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class PrincipalButton {
    private static final String ACTION_SELECTED_CLASS = "action-button-selected";
    private GameHUD gameHUD;
    public StackPane root;

    @FXML private Button attackButton;
    @FXML private Button travelButton;
    @FXML private Button powerUpButton;
    @FXML private AnchorPane actionContainer;
    @FXML private Label actionLabel;


    public void attack() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("attack")) return;
        GameController.getInstance().setPendingAction(GameController.PendingAction.ATTACK);
        setActionSelection(attackButton);
        showActionContainer("Attack", getCurrentZoneName());
    }

    public void travel() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("travel")) return;
        GameController.getInstance().setPendingAction(GameController.PendingAction.TRAVEL);
        setActionSelection(travelButton);
        showActionText("Travel to ....");
    }

    public void powerUp() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("power up")) return;
        GameController.getInstance().setPendingAction(GameController.PendingAction.POWER_UP);
        setActionSelection(powerUpButton);
        showActionContainer("Power up", getCurrentZoneName());
    }


    public void coalition() {
        if (gameHUD != null) {
            gameHUD.showCoalitionMenu();
        }
    }

    public void skipTurn() {
        if (!MultiplayerGameOverlay.ensureLocalTurn("skip turn")) return;
        GameController.getInstance().nextPlayer();
        clearActionSelection();
        hideActionContainer();
    }

    public void acceptAction() {
        hideActionContainer();


        QuestionController.setMainStackPane(root);
        if (GameController.getInstance().getPendingAction() == GameController.PendingAction.TRAVEL) {
            QuestionController.setDifficultyQuestion(GameController.getInstance().getCurrentDistance());

        } else {
            QuestionController.showChoiceDifficultPage();
        }
    }

    public void updateAttackAvailability(Player player) {
        if (attackButton == null) return;

        boolean disableAttack = true;
        if (player != null) {
            Zone currentZone = player.getCurrentZone();
            if (currentZone != null && currentZone.getNameOwner() != null) {
                disableAttack = currentZone.getNameOwner().equalsIgnoreCase(player.getPseudo());
            }
        }

        attackButton.setDisable(disableAttack);
        if (disableAttack && attackButton.getStyleClass().contains(ACTION_SELECTED_CLASS)) {
            clearActionSelection();
        }
    }

    private void setActionSelection(Button activeButton) {
        if (activeButton == null) return;

        attackButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        travelButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        powerUpButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        activeButton.getStyleClass().add(ACTION_SELECTED_CLASS);
    }

    private void clearActionSelection() {
        if (attackButton != null) {
            attackButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        }
        if (travelButton != null) {
            travelButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        }
        if (powerUpButton != null) {
            powerUpButton.getStyleClass().remove(ACTION_SELECTED_CLASS);
        }
    }

    public void resetActionSelection() {
        clearActionSelection();
        hideActionContainer();
    }

    private void showActionContainer(String action, String zoneName) {
        if (actionLabel != null) {
            if (zoneName == null || zoneName.isBlank()) {
                actionLabel.setText(action + " (Level 1 - 4)");
            } else {
                actionLabel.setText(action + " on " + zoneName + " (Level 1 - 4)");
            }
        }
        if (actionContainer != null) {
            actionContainer.setManaged(true);
            actionContainer.setVisible(true);
        }
    }

    public void updateTravelTarget(String zoneName, int distance, boolean isBoat) {
        if (zoneName == null || zoneName.isBlank() || distance <= 0) return;

        String transportType = isBoat ? "Boat" : "Horse-drawn";
        showActionText("Travel to " + zoneName + " via " + transportType + " (Level " + distance + ")");
    }

    private void showActionText(String text) {
        if (actionLabel != null) {
            actionLabel.setText(text);
        }
        if (actionContainer != null) {
            actionContainer.setManaged(true);
            actionContainer.setVisible(true);
        }
    }

    private void hideActionContainer() {
        if (actionContainer != null) {
            actionContainer.setVisible(false);
            actionContainer.setManaged(false);
        }
    }

    private String getCurrentZoneName() {
        GameController controller = GameController.getInstance();
        if (controller == null) return null;
        Player current = controller.getCurrentPlayer();
        if (current == null || current.getCurrentZone() == null) return null;
        return current.getCurrentZone().getName();
    }

    public void setParentRoot(StackPane root) {
        this.root = root ;
    }

    public void setGameHUD(GameHUD gameHUD) {
        this.gameHUD = gameHUD;
    }
}
