package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.ui.Notification;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationController {
	private static VBox notificationsHost = null;


	private NotificationController() { }

	public static void initialize() {
		if (notificationsHost != null) return;

        notificationsHost = new VBox(12);
        notificationsHost.setPickOnBounds(false);
        notificationsHost.setMouseTransparent(false);
        notificationsHost.setAlignment(Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(notificationsHost, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notificationsHost, new Insets(40, 40, 40, 40));
    }

	public static void show(String title, String message, Notification.Type type) {
		show(title, message, type, 7500);
	}

	public static void show(String title, String message, Notification.Type type, int ttlMillis) {
		if (notificationsHost == null) {
			System.err.println("Error: NotificationController not initialized. Call NotificationController.initialize() first.");
			return;
		}

		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(NotificationController.class.getResource(Constant.PATH + "ui/Notification.fxml"));
				AnchorPane notificationView = loader.load();
				Notification notificationController = loader.getController();

				notificationController.setTitleLabel(title);
				notificationController.setMessageLabel(message);
				notificationController.setType(type);
				notificationController.setTimeToLive(ttlMillis);

				notificationView.setTranslateX(400);
//				notificationView.setOpacity(0);

				notificationController.setOnClose(() -> {
					TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationView);
					slideOut.setByX(500);
					slideOut.setInterpolator(Interpolator.EASE_IN);

					FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationView);
					fadeOut.setToValue(0);

					ParallelTransition parallel = new ParallelTransition(slideOut, fadeOut);
					parallel.setOnFinished(e -> notificationsHost.getChildren().remove(notificationView));
					parallel.play();
				});

				notificationsHost.getChildren().addLast(notificationView);


				TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationView);
				slideIn.setToX(0);
				slideIn.setInterpolator(Interpolator.EASE_OUT);

//				FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationView);
//				fadeIn.setToValue(1);

//				new ParallelTransition(slideIn, fadeIn).play();
				slideIn.play();

				notificationController.startClosingAnimation();

			} catch (Exception e) {
				System.err.println("Error showing notification: " + e.getMessage());
			}
		});
	}


	public static VBox getNotificationsHost() {
		return notificationsHost;
	}
}
