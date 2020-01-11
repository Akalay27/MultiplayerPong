package com.adam.pong.game;

import java.awt.Color;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PongServer extends Thread {

    private byte[] buf = new byte[256];
    private boolean running;
    private DatagramSocket socket;
    public CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<Player>();
    private GameLoop gameLoop;
    private List<Integer> updatedPlayers = new ArrayList<>();
    private int idCounter = 0;

    public PongServer() throws SocketException, UnknownHostException {
        socket = new DatagramSocket(4445);
        System.out.println("Server started!");
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        gameLoop = new GameLoop(players);
        gameLoop.start();

    }

    public void run() {
        running = true;

        while (running) {

            if (gameLoop.needToRegenerateBounds()) {
                generatePlayerPositions();
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
                generatePlayerPositions();
            } else {

                // if it is a normal packet from an already registered player
                for (Player p : players) {
                    if (playerState.id == p.getId()) {
                        p.setInput(playerState.input);
                    }
                }
                // TODO: Use timestamps to ensure GameEvents are recorded by the client
                if (!updatedPlayers.contains(playerState.id)) {
                    Player playerArr[] = new Player[currentPlayers().size()];
                    playerArr = currentPlayers().toArray(playerArr);
                    buf = SerializationUtils.serialize(new GameState(playerArr, gameLoop.getBallPosition(), GameEvent.REORGANIZE_PLAYERS));
                    updatedPlayers.add(playerState.id);
                } else {
                    HashMap<Integer,Paddle> paddlePositions = new HashMap<>();
                    for (Player p : currentPlayers()) {
                        paddlePositions.put(p.getId(),p.getPaddle());
                    }
                    buf = SerializationUtils.serialize(new GameState(paddlePositions,gameLoop.getBallPosition(),GameEvent.NONE));
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

    public void generatePlayerPositions() {
        ArrayList<Player> cPlayers = currentPlayers();
        boolean randomOrder = false;
        double radius = 200/(2*Math.sin(Math.PI/cPlayers.size()));

        double[] center = {0,0};
        double incr = Math.PI*2/cPlayers.size();
        if (cPlayers.size() > 2) {
            gameLoop.setInvisibleBounds(-1);
            for (int p = 0; p < cPlayers.size(); p++) {
                double angle1 = p * incr;
                double angle2 = (p + 1) * incr;

                PlayerBounds playerBounds = new PlayerBounds(new Point2D((int) (Math.cos(angle1) * radius + center[0]), (int) (Math.sin(angle1) * radius + center[1])), new Point2D(
                        (int) (Math.cos(angle2) * radius + center[0]), (int) (Math.sin(angle2) * radius + center[1])));
                cPlayers.get(p).setPlayerBounds(playerBounds);
            }
        } else if (cPlayers.size() == 2) {
            double arenaScale = 4;
            PlayerBounds left = new PlayerBounds(new Point2D(radius*arenaScale,-radius/2*arenaScale),new Point2D(radius*arenaScale,radius/2*arenaScale));
            PlayerBounds right = new PlayerBounds(new Point2D(-radius*arenaScale,-radius/2*arenaScale),new Point2D(-radius*arenaScale,radius/2*arenaScale));
            cPlayers.get(0).setPlayerBounds(left);
            cPlayers.get(1).setPlayerBounds(right);
            gameLoop.setInvisibleBounds(radius*arenaScale);
        }




        updatedPlayers.clear();
    }

    public ArrayList<Player> currentPlayers() {
        ArrayList<Player> cPlayers = new ArrayList<>();
        for (Player p : players) {
            if (!p.isEliminated()) cPlayers.add(p);
        }

        return cPlayers;
    }





}
