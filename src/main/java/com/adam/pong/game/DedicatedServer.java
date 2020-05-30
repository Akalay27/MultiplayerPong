package com.adam.pong.game;

import com.adam.pong.game.CPPongClient;
import com.adam.pong.game.PongServer;

import java.io.IOException;
import java.net.InetAddress;

public class DedicatedServer {

    public static void main(String[] args) throws IOException {

        PongServer server = new PongServer();
        server.start();

        for (int p = 0; p < Integer.parseInt(args[0]); p++) {
            CPPongClient client = new CPPongClient(InetAddress.getLocalHost().getAddress(), 25565);
            client.start();
        }
    }

}
