module io.github.xen0rstar.historicconquest {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.xml;


    opens io.github.xen0rstar.historicconquest to javafx.fxml;
    exports io.github.xen0rstar.historicconquest;
}