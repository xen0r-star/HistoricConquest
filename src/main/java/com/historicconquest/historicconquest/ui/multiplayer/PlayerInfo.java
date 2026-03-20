package com.historicconquest.historicconquest.ui.multiplayer;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;

public class PlayerInfo {
    @FXML private Pane root;
    @FXML private Label PlayerName;
    @FXML private Label PlayerStatus;
    @FXML private Label PlayerPing;
    @FXML private ImageView PlayerIcon;
    @FXML private ImageView RemoveBtn;


    public Pane getRoot() {
        return root;
    }

    public void setPlayerName(String name) {
        if (PlayerName != null && name != null) {
            PlayerName.setText(name);
        }
    }

    public void setPlayerStatus(String status) {
        if (PlayerStatus != null && status != null) {
            PlayerStatus.setText(status);
        }
    }

    public void setPlayerPing(String ping) {
        if (PlayerPing != null && ping != null) {
            PlayerPing.setText(ping);
        }
    }

    public void setPlayerIcon(Image image) {
        if (PlayerIcon != null && image != null) {
            PlayerIcon.setImage(image);
        }
    }

    public void setPlayerIcon(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return;
        }

        URL resource = getClass().getResource(resourcePath);
        if (resource != null) {
            setPlayerIcon(new Image(resource.toExternalForm()));
        }
    }

    public void setOnClose(Runnable onClose) {
        if (RemoveBtn != null) {
            RemoveBtn.setOnMouseClicked(event -> {
                if (onClose != null) {
                    onClose.run();
                }
            });
        }
    }

    public void setOnNameClick(Runnable onNameClick) {
        if (PlayerName != null && onNameClick != null) {
            PlayerName.setCursor(Cursor.HAND);
            PlayerName.setOnMouseClicked(event -> onNameClick.run());
        }
    }

    public void removeOnRemove() {
        if (RemoveBtn != null) {
            ((Pane) RemoveBtn.getParent()).getChildren().remove(RemoveBtn);
        }
    }
}
