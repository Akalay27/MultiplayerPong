package com.adam.pong.game;

import org.apache.commons.lang3.SerializationUtils;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

public class PongClient extends Thread {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private byte[] buf;
    protected int id;
    private String name;
    protected Player[] players;
    protected Point2D ballPosition;
    private int bufferSize = 65536;
    private Point2D[] debugPoints;

    public PongClient (String name, byte[] address) throws SocketException, UnknownHostException {

        socket = new DatagramSocket();
        socket.setSoTimeout(500);
        serverAddress = InetAddress.getByAddress(address);
        this.name = name;

    }
    public PongClient() {};


    public void connectToServer() {
        DatagramPacket packet;
        while (true) {
            try {
                System.out.println("Sending join packet...");
                buf = SerializationUtils.serialize(new PlayerState(name));
                packet = new DatagramPacket(buf, buf.length, serverAddress, 4445);

                socket.send(packet);
                buf = new byte[bufferSize];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);



                break;
            } catch (IOException e) {
                System.out.println("Could not connect to server.");
                continue;
            }
        }

        Player newPlayer = SerializationUtils.deserialize(packet.getData());

        System.out.println("[" + name + "] Server has set my id to " + newPlayer.getId());
        this.id = newPlayer.getId();
    }

    public void update(UserInput userInput) throws IOException {

        PlayerState ps = new PlayerState(this.id, userInput);
        buf = SerializationUtils.serialize(ps);
        DatagramPacket packet = new DatagramPacket(buf,buf.length,serverAddress, 4445);
        socket.send(packet);

        buf = new byte[bufferSize];
        packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        GameState gs = SerializationUtils.deserialize(packet.getData());
        if (gs.event == GameEvent.REORGANIZE_PLAYERS) {
            players = gs.players;
            ballPosition = gs.ballPosition;
        } else if (gs.event == GameEvent.NONE) {
            for (Player p : players) {
                p.setPaddle(gs.paddlePositions.get(p.getId()));
            }
            ballPosition = gs.ballPosition;
            debugPoints = gs.debugPoints;
        }



    }

    public Player[] getPlayers() {
        return players;
    }
    public Point2D getBallPosition () {
        return ballPosition;
    }
    public void close() {
        socket.close();
    }

    public Point2D[] getDebugPoints() {
        return debugPoints;
    }

    public int getPlayerId() {
        return id;
    }
}