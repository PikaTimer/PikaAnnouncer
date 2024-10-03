module com.pikatimer.pikaannouncer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.pikatimer.pikaannouncer to javafx.fxml;
    exports com.pikatimer.pikaannouncer;
    
    requires org.json;
    requires  org.slf4j;
    requires org.java_websocket;
    requires java.prefs;
    requires org.controlsfx.controls;
    requires de.siegmar.fastcsv;

}
