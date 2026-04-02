module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.java_websocket;
    requires jjwt.api;
    requires java.xml;

    // EXPORTS
    exports com.historicconquest.historicconquest.app;
    exports com.historicconquest.historicconquest.controller;
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
    exports com.historicconquest.historicconquest.view;
    exports com.historicconquest.historicconquest.view.map;
    exports com.historicconquest.historicconquest.view.multiplayer;

    // JAVAFX
    opens com.historicconquest.historicconquest.app to javafx.fxml;
    opens com.historicconquest.historicconquest.controller to javafx.fxml;
    opens com.historicconquest.historicconquest.view to javafx.fxml;
    opens com.historicconquest.historicconquest.view.multiplayer to javafx.fxml;

    // JACKSON
    opens com.historicconquest.historicconquest.model.network.model to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.model.network.event to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.service.network to com.fasterxml.jackson.databind;
}