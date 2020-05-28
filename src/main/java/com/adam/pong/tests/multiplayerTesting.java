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

        for (int p = 0; p < 50; p++) {
            CPPongClient client = new CPPongClient("cpuClient"+p,InetAddress.getLocalHost().getAddress(),25565);
            client.start();

        }
    }
}
