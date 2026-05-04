package com.historicconquest.historicconquest.controller.core;

import com.historicconquest.historicconquest.controller.game.GameController;
import com.historicconquest.historicconquest.controller.game.MapBackgroundController;
import com.historicconquest.historicconquest.controller.overlay.HelpController;
import com.historicconquest.historicconquest.controller.overlay.NotificationController;
import com.historicconquest.historicconquest.controller.overlay.PauseGameController;
import com.historicconquest.historicconquest.controller.overlay.SettingsController;
import com.historicconquest.historicconquest.model.game.Game;
import com.historicconquest.historicconquest.service.network.RoomService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.EnumMap;

public class AppController {
	private static AppController instance;
	private final EnumMap<AppPage, String> pageFxml = new EnumMap<>(AppPage.class);

	private Stage stage;
	private StackPane root;
	private StackPane contentLayer;

	public AppController() {
		instance = this;
		pageFxml.put(AppPage.HOME, "/view/fxml/HomePage.fxml");
		pageFxml.put(AppPage.NEW_GAME, "/view/fxml/NewGame.fxml");
		pageFxml.put(AppPage.MULTIPLAYER, "/view/fxml/multiplayer/MultiplayerPage.fxml");
	}

	public static AppController getInstance() {
		return instance;
	}

	public void initialize(Stage stage, StackPane root) {
		this.stage = stage;
		this.root = root;

		contentLayer = new StackPane();
		root.getChildren().setAll(contentLayer);

		HelpController.initialize();
		SettingsController.initialize();
		NotificationController.initialize();
		MapBackgroundController.initialize();
		PauseGameController.initialize();

		addOverlay(SettingsController.getSettings());
		addOverlay(HelpController.getHelp());
		addOverlay(NotificationController.getNotifications());
		addOverlay(PauseGameController.getPauseGame());
	}

	public void showPage(AppPage page) {
		StackPane loadedPage = loadPage(page);
		if (loadedPage == null) return;

		contentLayer.getChildren().setAll(loadedPage);
		showHelp(false);
		showSettings(false);
	}

	public void showSettings(boolean show) {
		StackPane settings = SettingsController.getSettings();
		if (settings == null) return;

		addOverlay(settings);
		if (show) {
			SettingsController.show();
			settings.toFront();
			return;
		}

		SettingsController.close();
	}

	public void showHelp(boolean show) {
		StackPane help = HelpController.getHelp();
		if (help == null) return;

		addOverlay(help);
		if (show) {
			HelpController.show();
			help.toFront();
			return;
		}

		HelpController.close();
	}

	public void showPauseGame(boolean show) {
		if (GameController.getInstance() != null) {
			AnchorPane pauseGame = PauseGameController.getPauseGame();
			if (pauseGame == null) return;

			addOverlay(pauseGame);
			if (show) {
				PauseGameController.show();
				pauseGame.toFront();
				return;
			}

			PauseGameController.close();
		}
	}

	public void exit() {
		try {
			if (RoomService.isInitialized()) {
				RoomService.reset();
			}

			if (stage != null) {
				stage.close();
			}

			Platform.exit();
			System.exit(0);

		} catch (Exception e) {
			System.err.println("Error while exiting application: " + e.getMessage());
			System.exit(0);
		}
	}

	private StackPane loadPage(AppPage page) {
		String resource = pageFxml.get(page);
		if (resource == null) {
			System.err.println("No FXML mapping for page: " + page);
			return null;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
			return loader.load();

		} catch (Exception e) {
			System.err.println("Error loading page: " + page);
			return null;
		}
	}

	private void addOverlay(Node overlay) {
		if (overlay == null || root == null) return;

		if (overlay.getParent() instanceof Pane parent && parent != root) {
			parent.getChildren().remove(overlay);
		}

		if (!root.getChildren().contains(overlay)) {
			root.getChildren().add(overlay);
		}
	}
}
