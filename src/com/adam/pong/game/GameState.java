package com.adam.pong.game;



import java.io.Serializable;
import java.util.HashMap;

public class GameState implements Serializable {

    public GameEvent event;
    public Point2D ballPosition;
    public Player[] players;
    public HashMap<Integer,Paddle> paddlePositions;

    // normal packets, just sending all paddle positions and current ball position

    // sending normal packets but now with event (for graphical purposes)
    public GameState(Player[] players, Point2D ballPosition, GameEvent event) {
        this.players = players;
        this.ballPosition = ballPosition;
        this.event = event;
    }

    // for resetting the field after a player died, or when a game starts
    public GameState(Player[] players, GameEvent event) {
        this.players = players;
        this.event = event;
    }

    public GameState(HashMap<Integer,Paddle> paddlePositions, Point2D ballPosition, GameEvent event) {
        this.paddlePositions = paddlePositions;
        this.event = event;
        this.ballPosition = ballPosition;
    }



}

