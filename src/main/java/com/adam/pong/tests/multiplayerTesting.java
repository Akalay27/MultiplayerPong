package com.adam.pong.tests;

import com.adam.pong.game.CPPongClient;
import com.adam.pong.game.PongClient;
import com.adam.pong.game.PongServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class multiplayerTesting {

    public static void main(String[] args) throws IOException {

        PongServer server = new PongServer();
        server.start();
    }
}
