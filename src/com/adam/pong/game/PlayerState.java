package com.adam.pong.game;

import java.io.Serializable;

public class PlayerState implements Serializable {

    public int id;
    public String name;
    public UserInput input;

    // when first talking to the server
    public PlayerState(String name) {
        this.name = name;
        this.id = -1;
    }

    // normal packet structure during gameplay
    public PlayerState(int id, UserInput input) {
        this.id = id;
        this.input = input;
    }




}
