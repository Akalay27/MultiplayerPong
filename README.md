# MultiplayerPong / PolyPong

PolyPong is a game in which the player battles against a number of opponents. These opponents can either be computer controlled players or even other internet-connected players. The game takes place over a series of rounds. One player is eliminated each round from the ball touching their bounds, or the area that is on the sides of their paddle. The game restarts after only 1 player remains, making them the winner of the round.

I used JavaFX 14 for the graphics of the game, including the main menu GUI. I was able to build a JavaFX executable for both Mac and Windows using Maven.

I learned all about parallelism and networking through this project by making my own client/server structure, and making that structure as efficient as possible.

[Release download](https://github.com/Akalay27/MultiplayerPong/releases/)

[GitHub repo](https://github.com/Akalay27/MultiplayerPong)

Here's a short demo video:

https://www.youtube.com/embed/jm8afRW1JJU

In this demo, the server and all the computer-controlled players are being hosted on a separate Linux machine, and using the multiplayer mode in PolyPong, I am able to connect and play on the server using my PC. Playing with multiple human players and several bots on one server is fun!
