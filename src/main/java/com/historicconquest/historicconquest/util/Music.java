package com.historicconquest.historicconquest.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Music {
    private final List<String> tracks = new ArrayList<>();
    private int currentTrackIndex = 0;
    private static MediaPlayer mediaPlayer;


    private static Music instance;

    private Music() {
        for (int i = 1; i <= 6; i++) {
            tracks.add("/music/music" + i + ".mp3");
        }
    }

    public void playPlaylist() {
        if (currentTrackIndex >= tracks.size()) {
            currentTrackIndex = 0;
        }

        String path = tracks.get(currentTrackIndex);
        Media media = new Media(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setVolume(0.5);
        mediaPlayer.setMute(false);

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose();
            currentTrackIndex++;
            playPlaylist();
        });

        mediaPlayer.play();
    }

    public static void setVolume(double volume) {
        if (mediaPlayer != null) mediaPlayer.setVolume(volume);
    }

    public static void setMuted(boolean mute) {
        if (mediaPlayer != null) mediaPlayer.setMute(mute);
    }


    public static Music getInstance() {
        if (instance == null) {
            instance = new Music();
        }
        return instance;
    }
}
