package com.adam.udptesting;

import java.io.IOException;
import java.net.*;


public class EchoServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public EchoServer() throws SocketException, UnknownHostException {

        socket = new DatagramSocket(4445);
        System.out.println("New EchoServer with IP: " + InetAddress.getLocalHost().getHostAddress());
    }

    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /* After receiving packet, get info to send one back */
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

//            /* modifying the message before sending it back */
//            String msg = new String(packet.getData(),0,packet.getLength());
//            msg += " is really awesome.";
//            buf = msg.getBytes();

            packet = new DatagramPacket(buf, 0,buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());

            if (received.equals("end")) {
                running = false;
                continue;
            }

            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        socket.close();
    }

}
