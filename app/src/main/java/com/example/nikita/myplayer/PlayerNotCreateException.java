package com.example.nikita.myplayer;

public class PlayerNotCreateException extends Exception {
    private static final String description = "The player is not created. First, call the method AudioPlayer.create()";

    @Override
    public String toString(){
        return description;
    }
}
