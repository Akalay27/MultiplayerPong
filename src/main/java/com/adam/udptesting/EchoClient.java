package com.adam.udptesting;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class EchoClient extends Thread {

    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf = new byte[256];

    public EchoClient () throws SocketException, UnknownHostException {

        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");

    }

    public byte[] sendEcho(byte[] msg) throws IOException {

        buf = msg;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);

        buf = new byte[256]; // since incoming message might be larger than what we sent
        packet = new DatagramPacket(buf, buf.length);

        socket.receive(packet);

        return packet.getData();
    }

    public void close() {
        socket.close();
    }
}
