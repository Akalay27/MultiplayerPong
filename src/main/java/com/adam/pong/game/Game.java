package com.adam.pong.game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Game extends Application {
    private PongClient client;
    private PongServer server;
    private CPPongClient cpuClients[];
    private Stage primaryStage;
    private UserInput userInput;
    private GraphicsContext gc;
    private ResizableCanvas canvas;
    private boolean showPlayerNames;
    private final int menuWidth = 800;
    private final int menuHeight = 800;
    private AnimationTimer animator;
    private Scene mainMenu;

    private boolean isQuitting = false;
    private double quitTimer = 0;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
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
            if (ke.getCode() == KeyCode.DOWN) {
                userInput = UserInput.LEFT;
            }
            if (ke.getCode() == KeyCode.UP) {
                userInput = UserInput.RIGHT;
            }
            if (ke.getCode() == KeyCode.CONTROL) {
                showPlayerNames = true;
            }

            if (ke.getCode() == KeyCode.ESCAPE) {
                if (!isQuitting) quitTimer = System.currentTimeMillis();
                isQuitting = true;
            }
        });
        gameScene.setOnKeyReleased(ke -> {
            if (ke.getCode() == KeyCode.LEFT && userInput == UserInput.LEFT) {
                userInput = UserInput.NONE;
            }
            if (ke.getCode() == KeyCode.RIGHT && userInput == UserInput.RIGHT) {
                userInput = UserInput.NONE;
            }
            if (ke.getCode() == KeyCode.DOWN && userInput == UserInput.LEFT) {
                userInput = UserInput.NONE;
            }
            if (ke.getCode() == KeyCode.UP && userInput == UserInput.RIGHT) {
                userInput = UserInput.NONE;
            }
            if (ke.getCode() == KeyCode.CONTROL) {
                showPlayerNames = false;
            }
            if (ke.getCode() == KeyCode.ESCAPE) {
                isQuitting = false;
            }
        });

        /* Singleplayer Options Select Scene */
        VBox spOptions = new VBox(10);
        GridPane playerAmount = new GridPane();
        spOptions.setAlignment(Pos.CENTER);
        playerAmount.setAlignment(Pos.CENTER);
        TextField numPlayers = new TextField("");
        numPlayers.setId("numplayers");
        playerAmount.add(new Label("Number of Players: "), 1, 0);
        playerAmount.add(numPlayers, 2, 0);
        Button playButton = new Button("Start!");
        Button spToMain = new Button("Back to Menu");
        spToMain.setId("back-button");
        spToMain.setOnAction(e -> primaryStage.setScene(mainMenu));
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
        spOptions.getChildren().addAll(playerAmount,playButton, spToMain);
        Scene singlePlayerOptions = new Scene(spOptions, menuWidth, menuHeight);
        singlePlayerOptions.getStylesheets().add("menuStyle.css");

        /* Multiplayer Options Select Scene */
        VBox mpOptions = new VBox(10);
        GridPane ipPortName = new GridPane();
        mpOptions.setAlignment(Pos.CENTER);
        ipPortName.setAlignment(Pos.CENTER);
        TextField ip = new TextField("");
        TextField port = new TextField("");
        TextField name = new TextField("");
        ipPortName.add(new Label("Server IP:"), 1, 0);
        ipPortName.add(ip, 2, 0);
        ipPortName.add(new Label("Server Port:"), 1, 1);
        ipPortName.add(port, 2, 1);
        ipPortName.add(new Label("Player Name:"), 1, 2);
        ipPortName.add(name, 2, 2);
        Button joinButton = new Button("Join Game!");
        Button mpToMain = new Button("Back to Menu");
        mpToMain.setId("back-button");
        mpToMain.setOnAction(e -> primaryStage.setScene(mainMenu));

        joinButton.setOnAction(e -> {
            try {
                joinMultiPlayerGame(name.getCharacters().toString(),ip.getCharacters().toString(),port.getCharacters().toString());
                primaryStage.setScene(gameScene);
                primaryStage.setMaximized(true);
                startGame();
            } catch (UnknownHostException | SocketException unknownHostException) {
                unknownHostException.printStackTrace();
            }
        });

        mpOptions.getChildren().addAll(ipPortName,joinButton,mpToMain);
        Scene multiPlayerOptions = new Scene(mpOptions, menuWidth, menuHeight);
        multiPlayerOptions.getStylesheets().add("menuStyle.css");

        /* How to Play Scene */
        VBox textinfo = new VBox();
        textinfo.maxWidth(menuWidth*0.8);
        textinfo.fillWidthProperty();
        textinfo.setAlignment(Pos.CENTER);

        Text basicInfo = new Text("Welcome to PolyPong! \n" +
                "In PolyPong, you face a number of opponents in a polygonal shape and attempt to knock the ball into one of the opponents' bounds. \n" +
                "Your paddle will always be on the bottom of the screen, and on the right side of the screen on the last round.\n"
                );

        HBox ks1 = new HBox();
        Label ks1Label = new Label(" \uD83E\uDC08 ");
        Text ks1Text = new Text("Paddle Left ");
        ks1Label.setId("hotkey");
        ks1Text.setId("hotkey-text");
        Label ks2Label = new Label(" \uD83E\uDC0A ");
        Text ks2Text = new Text("Paddle Right ");
        ks2Label.setId("hotkey");
        ks2Text.setId("hotkey-text");
        ks1.getChildren().addAll(ks1Label,ks1Text, ks2Label, ks2Text);
        ks1.setAlignment(Pos.CENTER);
        ks1.setSpacing(10);
        ks1.setMaxWidth(menuWidth*0.8);

        HBox ks3 = new HBox();
        Label ks3Label = new Label(" CTRL ");
        Text ks3Text = new Text("Show Player Names");
        ks3Label.setId("hotkey");
        ks3Text.setId("hotkey-text");
        ks3.getChildren().addAll(ks3Label,ks3Text);
        ks3.setAlignment(Pos.CENTER);
        ks3.setSpacing(10);
        ks3.setMaxWidth(menuWidth*0.8);

        HBox ks4 = new HBox();
        Label ks4Label = new Label(" Hold ESC ");
        Text ks4Text = new Text("Back to Menu");
        ks4Label.setId("hotkey");
        ks4Text.setId("hotkey-text");
        ks4.getChildren().addAll(ks4Label,ks4Text);
        ks4.setAlignment(Pos.CENTER);
        ks4.setSpacing(10);
        ks4.setMaxWidth(menuWidth*0.8);

        basicInfo.setTextAlignment(TextAlignment.CENTER);
        basicInfo.setWrappingWidth(menuWidth*0.8);
        basicInfo.setId("instructions");
        textinfo.setSpacing(10);
        Button instToMain = new Button("Back to Menu");
        instToMain.setId("back-button");
        instToMain.setOnAction(e -> primaryStage.setScene(mainMenu));
        textinfo.getChildren().addAll(basicInfo,ks1,ks3,ks4,instToMain);
        Scene instructions = new Scene(textinfo, menuWidth, menuHeight);
        instructions.getStylesheets().add("menuStyle.css");


        /* Main Menu Scene */
        Button singlePlayer = new Button("Singleplayer");
        singlePlayer.setOnAction(e -> {
            primaryStage.setScene(singlePlayerOptions);
        });
        Button multiPlayer = new Button("Multiplayer");
        multiPlayer.setOnAction(e -> {
            primaryStage.setScene(multiPlayerOptions);
        });
        Button howToPlay = new Button("How to Play");
        howToPlay.setOnAction(e -> {
            primaryStage.setScene(instructions);
        });
        Text title = new Text("PolyPong");
        title.setId("title-text");
        Text credit = new Text("2020 Adam Kalayjian");
        credit.setId("credit-text");
        VBox menuButtons = new VBox(8);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.getChildren().addAll(title,singlePlayer,multiPlayer,howToPlay, credit);
        mainMenu = new Scene(menuButtons, menuWidth, menuHeight);
        mainMenu.getStylesheets().add("menuStyle.css");
        primaryStage.setScene(mainMenu);
        primaryStage.show();


    }

    private void joinMultiPlayerGame(String name, String ip, String port) throws UnknownHostException, SocketException {
        byte[] address = InetAddress.getByName(ip).getAddress();
        int serverPort = Integer.parseInt(port);
        client = new PongClient(name,address,serverPort);
        client.connectToServer();
    }
    private void createSinglePlayerGame(int numPlayers, boolean isLocalhost) throws SocketException, UnknownHostException {

        server = new PongServer();
        server.start();
        cpuClients = new CPPongClient[numPlayers-1];
        for (int p = 0; p < numPlayers-1; p++) {
            CPPongClient cpuClient = new CPPongClient(InetAddress.getLocalHost().getAddress(),25565);
            cpuClient.start();
            cpuClients[p] = cpuClient;
        }

        client = new PongClient("Player", InetAddress.getLocalHost().getAddress(), 25565);
        client.connectToServer();
    }

    private void startGame() {

        Camera camera = new Camera(gc);
        gc.setTextAlign(TextAlignment.CENTER);

        animator = new AnimationTimer() {

            @Override
            public void handle(long arg0) {

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                try {
                    client.update(userInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                camera.setTransform(client.getPlayers(), client.getPlayerId());
                for (Player p : client.getPlayers()) {
                    camera.drawPlayer(p);

                    if (showPlayerNames)
                        camera.drawPlayerName(p);
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
                        camera.addChatMessage(((ChatEvent) e).getMessage());
                    }
                    if (e instanceof DirectionIndicatorEvent) {
                        camera.setAngleDisplayTimer(System.currentTimeMillis());
                        camera.setBallAngles(((DirectionIndicatorEvent) e).getFinalAngle(),((DirectionIndicatorEvent) e).getStartAngle());
                    }

                    client.removeGraphicsEventFromPending(e.getUUID());
                }
                camera.drawMessage(client.getMessage());
                camera.drawParticles();
                camera.moveParticles();
                camera.drawChat();
                camera.updateChatMessages();
                camera.drawAngleDisplay();

                if (isQuitting && System.currentTimeMillis() - quitTimer > 1000) {
                    isQuitting = false;
                    backToMainMenu();
                }

            }
        };
        animator.start();
    }

    private void backToMainMenu() {
        primaryStage.setScene(mainMenu);
        primaryStage.setMaximized(false);
        if (cpuClients != null)
            for (CPPongClient cl : cpuClients) cl.setRunning(false);
        if (animator != null) animator.stop();
        if (client != null) client.close();
        if (server != null) server.close();
    }

    @Override
    public void stop() throws Exception {
        backToMainMenu();
        super.stop();
    }
}
