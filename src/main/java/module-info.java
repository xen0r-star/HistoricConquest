module com.historicconquest.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.xml;


    opens com.historicconquest.historicconquest to javafx.fxml;
    exports com.historicconquest.historicconquest;
}