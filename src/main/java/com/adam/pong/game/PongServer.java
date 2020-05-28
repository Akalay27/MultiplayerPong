package com.adam.pong.game;

import java.awt.Color;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import org.apache.commons.lang3.SerializationUtils;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PongServer extends Thread {

    private byte[] buf = new byte[256];
    private boolean running;
    private DatagramSocket socket;
    public CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList();
    private GameLoop gameLoop;
    private GameManager gameManager;
    private List<Integer> updatedPlayers = new ArrayList();
    private int idCounter = 0;

    public PongServer() throws SocketException, UnknownHostException {
        socket = new DatagramSocket(25565);

        System.out.println("Server started!");
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        gameLoop = new GameLoop(players);

        gameManager = new GameManager(players,gameLoop);
        gameManager.start();
        gameLoop.start();

    }

    public void run() {
        running = true;

        while (running) {

            if (gameManager.needToUpdatePlayers()) {
                updatedPlayers.clear();
            }

            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /* After receiving packet, get info to send one back */
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            PlayerState playerState = SerializationUtils.deserialize(packet.getData());

            if (playerState.id == -1) {
                Player newPlayer = createPlayerFromNewConnection(playerState);
                players.add(newPlayer);
                System.out.println(playerState.name + " joined the game.");
                buf = SerializationUtils.serialize(newPlayer);

            } else {

                // if it is a normal packet from an already registered player
                for (Player p : players) {
                    if (playerState.id == p.getId()) {
                        p.setInput(playerState.input);
                    }
                }

                if (!updatedPlayers.contains(playerState.id)) {
                    Player playerArr[] = new Player[currentPlayers().size()];
                    playerArr = currentPlayers().toArray(playerArr);
                    buf = SerializationUtils.serialize(new GameState(playerArr, gameLoop.getBallPosition(), GameEvent.REORGANIZE_PLAYERS,gameLoop.getDebugPoints(),gameManager.getMessage(), gameManager.getGraphicsEvents()));
                    updatedPlayers.add(playerState.id);
                } else {
                    HashMap<Integer,Paddle> paddlePositions = new HashMap();
                    for (Player p : currentPlayers()) {
                        paddlePositions.put(p.getId(),p.getPaddle());
                    }
                    //buf = SerializationUtils.serialize(new GameState(paddlePositions,gameLoop.getBallPosition(),GameEvent.NONE));
                    buf = SerializationUtils.serialize(new GameState(paddlePositions,gameLoop.getBallPosition(),GameEvent.NONE,gameLoop.getDebugPoints(),gameManager.getMessage(), gameManager.getGraphicsEvents()));
                }
            }

            packet = new DatagramPacket(buf, 0,buf.length, address, port);

            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Player createPlayerFromNewConnection(PlayerState ps) {
        Color randColor = Color.getHSBColor((float)Math.random(),1,1);
        Player player = new Player(idCounter,ps.name,randColor);
        idCounter += 1;
        return player;
    }

    public ArrayList<Player> currentPlayers() {
        ArrayList<Player> cPlayers = new ArrayList();
        for (Player p : players) {
            if (p.getState() == Player.State.INGAME) cPlayers.add(p);
        }

        return cPlayers;
    }







}
