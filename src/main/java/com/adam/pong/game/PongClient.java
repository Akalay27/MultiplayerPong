package com.adam.pong.game;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.UUID;

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

    private ArrayList<UUID> completedGraphicsEvents;
    private ArrayList<GraphicsEvent> pendingGraphicsEvents;



    public PongClient (String name, byte[] address, int port) throws SocketException, UnknownHostException {

        socket = new DatagramSocket();
        socket.setSoTimeout(500);
        serverAddress = InetAddress.getByAddress(address);
        this.name = name;
        serverPort = port;

        completedGraphicsEvents = new ArrayList<>();
        pendingGraphicsEvents = new ArrayList<>();

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
                System.out.println("Could not connect to server." + e);
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
            this.message = gs.message;
            for (GraphicsEvent e : gs.graphicsEvents) {
                addPendingGraphicsEvent(e);
            }
        } else if (gs.event == GameEvent.NONE) {
            for (Player p : players) {
                p.setPaddle(gs.paddlePositions.get(p.getId()));
            }
            ballPosition = gs.ballPosition;
            debugPoints = gs.debugPoints;
            this.message = gs.message;
            // extracting extra info from message
            for (GraphicsEvent e : gs.graphicsEvents) {
                addPendingGraphicsEvent(e);
            }
        }
    }

    public void minimalUpdate(UserInput userInput) throws IOException {

        PlayerState ps = new PlayerState(this.id, userInput);
        buf = SerializationUtils.serialize(ps);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
        socket.send(packet);

        buf = new byte[bufferSize];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        GameState gs = SerializationUtils.deserialize(packet.getData());
        if (gs.event == GameEvent.REORGANIZE_PLAYERS) {
            players = gs.players;
            ballPosition = gs.ballPosition;
        } else if (gs.event == GameEvent.NONE) {
            ballPosition = gs.ballPosition;
            for (Player p : players) {
                p.setPaddle(gs.paddlePositions.get(p.getId()));
            }
        }
    }

    private void addPendingGraphicsEvent(GraphicsEvent e) {
        for (GraphicsEvent event : pendingGraphicsEvents) {
            if (event.getUUID().equals(e.getUUID()))
                return;
        }
        for (UUID id : completedGraphicsEvents) {
            if (id.equals(e.getUUID()))
                return;
        }
        pendingGraphicsEvents.add(e);
        System.out.println("New event added with UUID" + e.getUUID());
    }

    public void removeGraphicsEventFromPending(UUID id) {
        completedGraphicsEvents.add(id);

        pendingGraphicsEvents.removeIf(e -> e.getUUID().equals(id));
    }

    public GraphicsEvent[] getPendingGraphicsEvents() {
        return pendingGraphicsEvents.toArray(new GraphicsEvent[0]);
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



}