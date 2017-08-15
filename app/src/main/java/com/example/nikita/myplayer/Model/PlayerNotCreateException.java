package com.example.nikita.myplayer.Model;

public class PlayerNotCreateException extends Exception {
    private static final String description = "The player is not created. First, call AudioPlayer.create()";

    @Override
    public String toString(){
        return description;
    }
}
