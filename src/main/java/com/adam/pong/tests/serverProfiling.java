package com.adam.pong.tests;

import com.adam.pong.game.CPPongClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class serverProfiling {

    public static void main(String[] args) throws IOException {
        for (int p = 0; p < 30; p++) {
            CPPongClient client = new CPPongClient("cpuClient"+p, InetAddress.getLocalHost().getAddress(),25565);
            client.start();

        }
    }
}
