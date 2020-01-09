package com.adam.pong.tests;

import com.adam.pong.game.PongClient;
import com.adam.pong.game.PongServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class multiplayerTesting {

    public static void main(String[] args) throws IOException {

        PongClient client1 = new PongClient("adam", InetAddress.getLocalHost().getAddress());
        PongClient client2 = new PongClient("bob", InetAddress.getLocalHost().getAddress());
        PongClient client3 = new PongClient("nick", InetAddress.getLocalHost().getAddress());
        PongClient client4 = new PongClient("garrett", InetAddress.getLocalHost().getAddress());
        PongClient client5 = new PongClient("adam", InetAddress.getLocalHost().getAddress());
        PongClient client6 = new PongClient("bob", InetAddress.getLocalHost().getAddress());
        PongClient client7 = new PongClient("nick", InetAddress.getLocalHost().getAddress());
        PongClient client8 = new PongClient("garrett", InetAddress.getLocalHost().getAddress());


        PongServer server = new PongServer();
        server.start();

        client1.connectToServer();
        client2.connectToServer();
        client3.connectToServer();
        client4.connectToServer();
        client5.connectToServer();
        client6.connectToServer();
        client7.connectToServer();
        client8.connectToServer();



    }
}
