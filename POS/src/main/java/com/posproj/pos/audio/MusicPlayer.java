package com.posproj.pos.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class MusicPlayer {
    public static boolean muted = false;
    private static MediaPlayer mediaPlayer;

    public static void playLoop() {
        if (mediaPlayer != null) return;

        URL resource = MusicPlayer.class.getResource("/sounds/relaxing.mp3");
        if (resource == null) {
            System.out.println("Music file not found!");
            return;
        }

        Media media = new Media(resource.toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setVolume(0.2);
        mediaPlayer.play();
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public static void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public static void toggleMute() {
        if (mediaPlayer == null) return;

        if (!muted) {
            mediaPlayer.setVolume(0);
            muted = true;
        } else {
            mediaPlayer.setVolume(0.2);
            muted = false;
        }
    }

    public static boolean isMuted() {
        return muted;
    }
}
