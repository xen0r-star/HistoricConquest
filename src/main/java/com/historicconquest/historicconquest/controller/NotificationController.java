package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.app.Constant;
import com.historicconquest.historicconquest.view.Notification;
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
	private static VBox root = null;


	private NotificationController() { }

	public static void initialize() {
		if (root != null) return;

		root = new VBox(12);
		root.setPickOnBounds(false);
		root.setMouseTransparent(false);
		root.setAlignment(Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(root, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(root, new Insets(40, 40, 40, 40));
    }

	public static void show(String title, String message, Notification.Type type) {
		show(title, message, type, 7500);
	}

	public static void show(String title, String message, Notification.Type type, int ttlMillis) {
		if (root == null) {
			System.err.println("[" + type.toString() + "] " + message);
			return;
		}

		Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(NotificationController.class.getResource(Constant.PATH + "view/fxml/Notification.fxml"));
				AnchorPane notificationView = loader.load();
				Notification notificationController = loader.getController();

				notificationController.setTitleLabel(title);
				notificationController.setMessageLabel(message);
				notificationController.setType(type);
				notificationController.setTimeToLive(ttlMillis);

				notificationView.setTranslateX(400);

				notificationController.setOnClose(() -> {
					TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationView);
					slideOut.setByX(500);
					slideOut.setInterpolator(Interpolator.EASE_IN);

					FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationView);
					fadeOut.setToValue(0);

					ParallelTransition parallel = new ParallelTransition(slideOut, fadeOut);
					parallel.setOnFinished(e -> root.getChildren().remove(notificationView));
					parallel.play();
				});

				root.getChildren().addLast(notificationView);


				TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationView);
				slideIn.setToX(0);
				slideIn.setInterpolator(Interpolator.EASE_OUT);
				slideIn.play();

				notificationController.startClosingAnimation();

			} catch (Exception e) {
				System.err.println("[" + type.toString() + "] " + message);
			}
		});
	}


	public static VBox getNotifications() {
		return root;
	}
}
