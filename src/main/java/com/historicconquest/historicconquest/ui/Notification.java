package com.historicconquest.historicconquest.ui;

import com.historicconquest.historicconquest.Constant;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.Objects;

public class Notification {
    public enum Type {
        SUCCESS,
        INFORMATION,
        ALERT,
        ERROR
    }


    @FXML AnchorPane Root;
    @FXML ImageView Icon;
    @FXML Label Title;
    @FXML Label Message;
    @FXML Pane ClosingBar;

    private int timeToLive = 7500;
    private Timeline closingTimeline;
    private Runnable onClose;
    private boolean closed;


    public void setTitleLabel(String title) {
        Title.setText(title);
    }

    public void setMessageLabel(String message) {
        Message.setText(message);
    }

    public void setType(Type type) {
        Root.getStyleClass().clear();
        Root.getStyleClass().add("button-small");
        Root.getStyleClass().add("not-hover");

        switch (type) {
            case SUCCESS:
                Root.getStyleClass().add("button-green");
                Icon.setImage(loadIcon("success-icon.png"));
                break;

            case INFORMATION:
                Root.getStyleClass().add("button-blue");
                Icon.setImage(loadIcon("information-icon.png"));
                break;

            case ALERT:
                Root.getStyleClass().add("button-yellow");
                Icon.setImage(loadIcon("alert-icon.png"));
                break;

            case ERROR:
                Root.getStyleClass().add("button-red");
                Icon.setImage(loadIcon("error-close.png"));
                break;
        }
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }


    public void startClosingAnimation() {
        ClosingBar.setPrefWidth(350.0);

        KeyValue kv = new KeyValue(
            ClosingBar.prefWidthProperty(),
            0,
            Interpolator.LINEAR
        );

        KeyFrame kf = new KeyFrame(Duration.millis(timeToLive), kv);

        closingTimeline = new Timeline(kf);
        closingTimeline.setOnFinished(e -> closeNotification());
        closingTimeline.play();
    }

    public void closeNotification() {
        if (closed) return;
        closed = true;

        if (closingTimeline != null) closingTimeline.stop();
        if (onClose != null) onClose.run();
    }

    private Image loadIcon(String fileName) {
        return new Image(Objects.requireNonNull(
            getClass().getResourceAsStream(Constant.PATH + "images/" + fileName)
        ));
    }
}
