package com.adam.pong.game;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.*;

public class PongClient extends Thread {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private byte[] buf;
    protected int id;
    private String name;
    protected Player[] players;
    protected Point2D ballPosition;
    private int bufferSize = 65536;
    private Point2D[] debugPoints;
    private String message;
    private String[] otherInfo;
    public PongClient (String name, byte[] address, int port) throws SocketException, UnknownHostException {

        socket = new DatagramSocket();
        socket.setSoTimeout(500);
        serverAddress = InetAddress.getByAddress(address);
        this.name = name;
        serverPort = port;

    }
    public PongClient() {};


    public void connectToServer() {
        DatagramPacket packet;
        while (true) {
            try {
                System.out.println("Sending join packet...");
                buf = SerializationUtils.serialize(new PlayerState(name));
                packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);

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
        DatagramPacket packet = new DatagramPacket(buf,buf.length,serverAddress, serverPort);
        socket.send(packet);

        buf = new byte[bufferSize];
        packet = new DatagramPacket(buf,buf.length);
        socket.receive(packet);
        GameState gs = SerializationUtils.deserialize(packet.getData());
        if (gs.event == GameEvent.REORGANIZE_PLAYERS) {
            players = gs.players;
            ballPosition = gs.ballPosition;
            handleMessage(gs.message);
        } else if (gs.event == GameEvent.NONE) {
            for (Player p : players) {
                p.setPaddle(gs.paddlePositions.get(p.getId()));
            }
            ballPosition = gs.ballPosition;
            debugPoints = gs.debugPoints;
            handleMessage(gs.message);
            // extracting extra info from message

        }



    }

    private void handleMessage(String message) {
        if (message.contains("?")) {
            this.message = message.substring(0, message.indexOf("?"));
            otherInfo = message.substring(message.indexOf('?') + 1).split("\\?", -1);
        } else {
            this.message = message;
            otherInfo = null;
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

    public String getMessage() {
        return message;
    }

    public String[] getOtherInfo() {
        return otherInfo;
    }
}