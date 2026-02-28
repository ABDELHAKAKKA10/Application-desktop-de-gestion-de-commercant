module com.example.commercant_gestion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.commercant_gestion to javafx.fxml;
    opens com.example.commercant_gestion.controller to javafx.fxml;
    opens com.example.commercant_gestion.model to javafx.base;

    exports com.example.commercant_gestion;
}