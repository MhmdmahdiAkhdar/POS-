package com.posproj.pos;

import com.posproj.pos.db.DatabaseConnection;
import com.posproj.pos.ui.Login;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseConnection.open();
        new Login(stage);
        com.posproj.pos.audio.MusicPlayer.playLoop();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
