module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.server to javafx.fxml;
    exports com.example.server;
}