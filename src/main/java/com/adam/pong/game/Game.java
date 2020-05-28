package com.adam.pong.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

public class Game extends Application {
    private PongClient client;
    private UserInput userInput;
    private Camera camera;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //getByName("107.129.94.149")

        client = new PongClient("adam", InetAddress.getByName("localhost").getAddress(), 25565);
        client.connectToServer();


        primaryStage.setTitle("PolyPong");
        Group root = new Group();
        final ResizableCanvas canvas = new ResizableCanvas();
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());

        final GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        camera = new Camera(gc);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(new Font(null, 45));

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.LEFT) {
                    userInput = UserInput.LEFT;
                }
                if (ke.getCode() == KeyCode.RIGHT) {
                    userInput = UserInput.RIGHT;
                }
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.LEFT && userInput == UserInput.LEFT) {
                    userInput = UserInput.NONE;
                }
                if (ke.getCode() == KeyCode.RIGHT && userInput == UserInput.RIGHT) {
                    userInput = UserInput.NONE;
                }
            }
        });

       // camera.drawShatteredLine(new Point2D(-100, 0), new Point2D(100, 0), new Point2D(0, 0));
        AnimationTimer animator = new AnimationTimer() {

            @Override
            public void handle(long arg0) {

                //  camera.setGraphicsContext(canvas.getGraphicsContext2D());
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

//                    if (!client.getMessage().equals("")) {
//                        camera.drawPlayerName(p, 1);
//                    }
                }

                System.out.println(getOtherInfo("sr"));
//                if (getOtherInfo("sr") != null) {
//                    camera.drawAngleDisplay(Double.parseDouble(Objects.requireNonNull(getOtherInfo("sr"))));
//                }

                camera.drawBall(client.getBallPosition());
                if (client.getDebugPoints() != null) {
                    camera.drawPoints(client.getDebugPoints(), 10);
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
// http://www.java-gaming.org/topics/getting-started-with-javafx-game-programming-for-java-programmers/37201/view.htm
}
