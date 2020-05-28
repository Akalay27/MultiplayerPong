package com.adam.pong.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javafx.scene.paint.Color;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

public class Game extends Application {
    private PongClient client;
    private UserInput userInput;
    private GraphicsContext gc;
    private ResizableCanvas canvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //getByName("107.129.94.149")

        /* Game Scene */
        primaryStage.setTitle("PolyPong");

        Group onlyCanvas = new Group();
        canvas = new ResizableCanvas();
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());
        gc = canvas.getGraphicsContext2D();
        onlyCanvas.getChildren().add(canvas);
        Scene gameScene = new Scene(onlyCanvas);
        gameScene.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.LEFT) {
                userInput = UserInput.LEFT;
            }
            if (ke.getCode() == KeyCode.RIGHT) {
                userInput = UserInput.RIGHT;
            }
        });
        gameScene.setOnKeyReleased(ke -> {
            if (ke.getCode() == KeyCode.LEFT && userInput == UserInput.LEFT) {
                userInput = UserInput.NONE;
            }
            if (ke.getCode() == KeyCode.RIGHT && userInput == UserInput.RIGHT) {
                userInput = UserInput.NONE;
            }
        });

        /* Singleplayer Options Select Scene */
        VBox spOptions = new VBox(10);
        GridPane playerAmount = new GridPane();
        spOptions.setAlignment(Pos.CENTER);
        playerAmount.setAlignment(Pos.CENTER);
        TextField numPlayers = new TextField("");
        playerAmount.add(new Label("Number of Players: "), 1, 0);
        playerAmount.add(numPlayers, 2, 0);
        Button playButton = new Button("Start!");
        playButton.setOnAction(e -> {
            try {
                createSinglePlayerGame(Integer.parseInt(numPlayers.getCharacters().toString()),false);
                primaryStage.setScene(gameScene);
                primaryStage.setMaximized(true);
                startGame();
            } catch (SocketException | UnknownHostException socketException) {
                socketException.printStackTrace();
            }
        });
        spOptions.getChildren().addAll(playerAmount,playButton);
        Scene singlePlayerOptions = new Scene(spOptions, 1000,1000);
        singlePlayerOptions.getStylesheets().add("menuStyle.css");

        /* Main Menu Scene */
        Button singlePlayer = new Button("Singleplayer");
        singlePlayer.setOnAction(e -> {
            primaryStage.setScene(singlePlayerOptions);
        });
        Button multiPlayer = new Button("Multiplayer");
        Button howToPlay = new Button("How to Play");
        VBox menuButtons = new VBox(8);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.getChildren().addAll(singlePlayer,multiPlayer,howToPlay);
        Scene mainMenu = new Scene(menuButtons, 1000,1000);
        mainMenu.getStylesheets().add("menuStyle.css");
        primaryStage.setScene(mainMenu);
        primaryStage.show();

//
//        client = new PongClient("adam", InetAddress.getByName("localhost").getAddress(), 25565);
//        client.connectToServer();

        //startGame();

    }

    private void createSinglePlayerGame(int numPlayers, boolean isLocalhost) throws SocketException, UnknownHostException {

        PongServer server = new PongServer();
        server.start();

        for (int p = 0; p < numPlayers-1; p++) {
            CPPongClient cpuClient = new CPPongClient("cpuClient"+p,InetAddress.getLocalHost().getAddress(),25565);
            cpuClient.start();
        }

        client = new PongClient("Player", InetAddress.getLocalHost().getAddress(), 25565);
        client.connectToServer();
    }

    private void startGame() {

        Camera camera = new Camera(gc);
        gc.setTextAlign(TextAlignment.CENTER);

        AnimationTimer animator = new AnimationTimer() {

            @Override
            public void handle(long arg0) {

//                gc.setGlobalAlpha(0.3);
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//                gc.setFill(Color.WHITE);
//                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                try {
                    client.update(userInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                camera.setTransform(client.getPlayers(), client.getPlayerId());
                for (Player p : client.getPlayers()) {
                    camera.drawPlayer(p);
                }


                camera.drawBall(client.getBallPosition());
                if (client.getDebugPoints() != null) {
                    camera.drawPoints(client.getDebugPoints(), 10);
                }
                GraphicsEvent[] graphicsEvents = client.getPendingGraphicsEvents();

                for (GraphicsEvent e : graphicsEvents) {
                    if (e instanceof DeathEvent) {
                        PlayerBounds bounds = ((DeathEvent) e).getBounds();
                        camera.drawShatteredLine(bounds.pt1, bounds.pt2, ((DeathEvent) e).getFocus());
                    }
                    if (e instanceof ChatEvent) {
                        System.out.println(((ChatEvent) e).getMessage());
                    }
                    client.removeGraphicsEventFromPending(e.getUUID());
                }
                camera.drawMessage(client.getMessage());
                camera.drawParticles();
                camera.moveParticles();
            }
        };
        animator.start();
    }

    private String getOtherInfo(String key) {
        if (client.getOtherInfo() != null) {
            for (String s : client.getOtherInfo()) {
                if (s.substring(0, s.indexOf("=")).equals(key)) {
                    return s.substring(s.indexOf("=") + 1);
                }
            }
        }
        return null;
    }
}
