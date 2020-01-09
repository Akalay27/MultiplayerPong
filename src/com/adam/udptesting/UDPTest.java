package com.adam.udptesting;

import java.io.IOException;

import org.apache.commons.lang3.*;
import java.util.Scanner;

public class UDPTest {

    EchoClient client;

    public static void main(String[] args) throws IOException {
        new EchoServer().start();
        EchoClient client = new EchoClient();
        Scanner input = new Scanner(System.in);
        Dog echo;
//        do {
//            echo = client.sendEcho(input.nextLine());
//            System.out.println(echo);
//        } while (echo != "END");
        Dog monty = new Dog("max","kchwaiuhdiuwa",2);
        echo = SerializationUtils.deserialize(client.sendEcho(SerializationUtils.serialize(monty)));
        echo.hello();
    }

}
