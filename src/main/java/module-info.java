module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires tools.jackson.core;
    requires org.java_websocket;
    requires jjwt.api;
    requires java.xml;
    requires tools.jackson.databind;
    requires com.fasterxml.jackson.core;

    // EXPORTS
    exports com.historicconquest.historicconquest.app;
    exports com.historicconquest.historicconquest.controller.core;
    exports com.historicconquest.historicconquest.controller.game;
    exports com.historicconquest.historicconquest.controller.overlay;
    exports com.historicconquest.historicconquest.controller.page;
    exports com.historicconquest.historicconquest.controller.page.multiplayer;
    exports com.historicconquest.historicconquest.controller.page.local;
    exports com.historicconquest.historicconquest.model.game;
    exports com.historicconquest.historicconquest.model.map;
    exports com.historicconquest.historicconquest.model.network.event;
    exports com.historicconquest.historicconquest.model.network.model;
    exports com.historicconquest.historicconquest.model.player;
    exports com.historicconquest.historicconquest.model.questions;
    exports com.historicconquest.historicconquest.service.map;
    exports com.historicconquest.historicconquest.service.network;
    exports com.historicconquest.historicconquest.util;
    exports com.historicconquest.historicconquest.util.tools;
    exports com.historicconquest.historicconquest.view.map;

    // JAVAFX
    opens com.historicconquest.historicconquest.app to javafx.fxml;
    opens com.historicconquest.historicconquest.controller.game to javafx.fxml;
    opens com.historicconquest.historicconquest.controller.overlay to javafx.fxml;
    opens com.historicconquest.historicconquest.controller.page to javafx.fxml;
    opens com.historicconquest.historicconquest.controller.page.multiplayer to javafx.fxml;
    opens com.historicconquest.historicconquest.controller.page.local to javafx.fxml;

    // JACKSON
    opens com.historicconquest.historicconquest.model.network.model to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.model.network.event to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.service.network to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.controller.core to javafx.fxml;
    exports com.historicconquest.historicconquest.controller.page.game;
    opens com.historicconquest.historicconquest.controller.page.game to javafx.fxml;
}