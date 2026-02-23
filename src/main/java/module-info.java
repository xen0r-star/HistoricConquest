module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires java.xml;

    opens com.historicconquest.historicconquest to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest;
}