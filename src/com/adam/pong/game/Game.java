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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;

public class Game extends Application {
    private PongClient client;
    private UserInput userInput;
    private Camera camera;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        client = new PongClient("adam", InetAddress.getByName("localhost").getAddress());
        client.connectToServer();


        primaryStage.setTitle("PolyPong");
        Group root = new Group();
        ResizableCanvas canvas = new ResizableCanvas();
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());

        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        camera = new Camera(gc);


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

        AnimationTimer animator = new AnimationTimer() {

            @Override
            public void handle(long arg0) {

                //  camera.setGraphicsContext(canvas.getGraphicsContext2D());

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                try {
                    client.update(userInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                camera.setTransform(client.getPlayers(), client.getPlayerId());
                for (Player p : client.getPlayers()) {
                    camera.drawPlayer(gc, p);
                }


                camera.drawBall(client.getBallPosition());
                if (client.getDebugPoints() != null) {
                    camera.drawPoints(gc, client.getDebugPoints(), 10);
                }
            }
        };

        animator.start();

    }





// http://www.java-gaming.org/topics/getting-started-with-javafx-game-programming-for-java-programmers/37201/view.htm



}
