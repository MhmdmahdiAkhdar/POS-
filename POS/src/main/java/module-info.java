module com.posproj.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;

    opens com.posproj.pos.models to javafx.base;
    opens com.posproj.pos to javafx.fxml;
    exports com.posproj.pos;
}