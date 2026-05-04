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

    private static boolean isMuted = false;
    private static double currentVolume = 0.5;

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

        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setVolume(currentVolume);
        mediaPlayer.setMute(isMuted);

        mediaPlayer.setOnEndOfMedia(() -> {
            currentTrackIndex++;
            playPlaylist();
        });

        mediaPlayer.play();
    }

    public static void setVolume(double volume) {
        currentVolume = volume;
        if (mediaPlayer != null) mediaPlayer.setVolume(volume);
    }

    public static void setMuted(boolean mute) {
        isMuted = mute;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);

            if (mute) mediaPlayer.pause();
            else mediaPlayer.play();
        }
    }


    public static Music getInstance() {
        if (instance == null) {
            instance = new Music();
        }
        return instance;
    }
}
