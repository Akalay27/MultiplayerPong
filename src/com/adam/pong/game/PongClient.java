package com.adam.pong.game;

import org.apache.commons.lang3.SerializationUtils;

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

    public PongClient (String name, byte[] address) throws SocketException, UnknownHostException {

        socket = new DatagramSocket();
        serverAddress = InetAddress.getByAddress(address);
        this.name = name;

    }
    public PongClient() {};


    public void connectToServer() throws IOException {

        buf = SerializationUtils.serialize(new PlayerState(name));
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, 4445);
        socket.send(packet);

        buf = new byte[bufferSize];
        packet = new DatagramPacket(buf, buf.length);

        socket.receive(packet);

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
        System.out.println("Packet size: " + packet.getLength());
        GameState gs = SerializationUtils.deserialize(packet.getData());
        if (gs.event == GameEvent.REORGANIZE_PLAYERS) {
            players = gs.players;
            ballPosition = gs.ballPosition;
        } else if (gs.event == GameEvent.NONE) {
            for (Player p : players) {
                p.setPaddle(gs.paddlePositions.get(p.getId()));
            }
            ballPosition = gs.ballPosition;
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


    public int getPlayerId() {
        return id;
    }
}