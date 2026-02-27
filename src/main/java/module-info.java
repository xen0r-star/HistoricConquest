module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires java.xml;

    opens com.historicconquest.historicconquest to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest;
    exports com.historicconquest.historicconquest.map;
    opens com.historicconquest.historicconquest.map to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest.questions;
    opens com.historicconquest.historicconquest.questions to javafx.fxml, javafx.graphics;
    exports com.historicconquest.historicconquest.ui;
    opens com.historicconquest.historicconquest.ui to javafx.fxml, javafx.graphics;
}