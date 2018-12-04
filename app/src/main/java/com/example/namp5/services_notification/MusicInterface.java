package com.example.namp5.services_notification;

/**
 * Created by namp5 on 11/29/2018.
 */

public interface MusicInterface {
    void create(int index);
    void start();
    void pause();
    boolean isPlaying();
    int getDuration();
    int getCurrrentPosition();
    void changeSong(int i);
    void seek(int possition);
}
