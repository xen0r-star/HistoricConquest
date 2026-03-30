module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.xml;
    requires org.java_websocket;
    requires jjwt.api;

    opens com.historicconquest.historicconquest to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest;
    exports com.historicconquest.historicconquest.map;
    opens com.historicconquest.historicconquest.map to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest.questions;
    opens com.historicconquest.historicconquest.questions to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest.ui;
    opens com.historicconquest.historicconquest.ui to javafx.fxml, javafx.graphics;
    opens com.historicconquest.historicconquest.ui.multiplayer to javafx.fxml, javafx.graphics;
    opens com.historicconquest.historicconquest.network.api to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.network.stomp to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.network.model to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.network.event to com.fasterxml.jackson.databind;
    opens com.historicconquest.historicconquest.network.service to com.fasterxml.jackson.databind;
    exports com.historicconquest.historicconquest.controller;
    opens com.historicconquest.historicconquest.controller to javafx.fxml, javafx.graphics;
}