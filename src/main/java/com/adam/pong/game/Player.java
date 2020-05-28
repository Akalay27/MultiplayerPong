package com.adam.pong.game;

import java.awt.*;
import java.io.Serializable;

public class Player implements Serializable {

    public enum State {
        INGAME, ELIMINATED, DEATH
    }
    private int id;
    private String name;
    private PlayerBounds playerBounds;
    private Paddle paddle;
    private double position;
    private UserInput input;
    private Color color;
    private State state;

    public Player(int id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.position = 0.5;
        this.color = color;
        this.state = State.ELIMINATED;
    }

    public PlayerBounds getPlayerBounds() {
        return playerBounds;
    }
    public void setPlayerBounds(PlayerBounds playerBounds) {
        this.playerBounds = playerBounds;
    }

    public int getId() {
        return id;
    }

    public Paddle getPaddle() {
        return paddle;
    }
    public void setPaddle(Paddle paddle) {
        this.paddle = paddle;
    }

    public UserInput getInput() {
        return input;
    }
    public void setInput(UserInput input) {
        this.input = input;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }
    public void changePosition(double change) {
        this.position += change;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }
}
