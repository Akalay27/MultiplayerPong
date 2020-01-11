package com.adam.pong.game;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

// TODO: Add CPU client
public class CPPongClient extends PongClient {

    boolean running = false;

    public CPPongClient (String name, byte[] address) throws SocketException, UnknownHostException {
        super(name,address);
    }
    @Override
    public void run() {

        running = true;
        try {
            connectToServer();
            update(UserInput.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (running) {

            Paddle paddle = null;
            PlayerBounds bounds = null;
            UserInput userInput = UserInput.NONE;

            boolean inGame = false;
            for (int p = 0; p < players.length; p++) {
                if (players[p].getId() == id) {
                    paddle = players[p].getPaddle();
                    bounds = players[p].getPlayerBounds();
                    inGame = true;
                }
            }
            if (!inGame) {
                running = false;
                continue;
            }
            Point2D paddleCenter = paddle.getCenter();
            Point2D projPointL = PongUtils.projectPointOnLine(bounds.pt1,paddleCenter,ballPosition);
            Point2D projPointR = PongUtils.projectPointOnLine(paddleCenter,bounds.pt2,ballPosition);


            boolean onLeft = PongUtils.isPointOnSegment(paddleCenter,bounds.pt1,projPointL,0.001);
            boolean onRight = PongUtils.isPointOnSegment(paddleCenter,bounds.pt2,projPointR,0.001);
            if (onLeft) userInput = UserInput.RIGHT;
            if (onRight) userInput = UserInput.LEFT;

            // TODO: Add variable accepted ball impact range so they don't always land right in middle. Use a timer with a random delay or something
            if (Math.random() > 0.5) {
                int choice = (int)(Math.random()*3);
                switch (choice) {
                    case 0:
                        userInput = UserInput.RIGHT;
                        break;
                    case 1:
                        userInput = UserInput.LEFT;
                        break;
                    case 2:
                        userInput = UserInput.NONE;
                        break;
                }
            }

            try {
                update(userInput);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


}
