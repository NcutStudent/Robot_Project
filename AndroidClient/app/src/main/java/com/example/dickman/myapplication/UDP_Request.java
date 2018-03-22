package com.example.dickman.myapplication;

import android.util.Log;

import java.io.IOException; //例外功能
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDP_Request {
    private String Host;
    private int SentPort;
    private DatagramSocket socket;
    private DatagramPacket rceivePacket;

    public UDP_Request(DatagramSocket socket, String Host, int SentPort, int timeout, int bufferSize) throws IOException {
        byte receiveBuffer[] = new byte[bufferSize];
        this.Host = Host;
        this.SentPort = SentPort;
        this.socket = socket;
        if (timeout != 0)
            socket.setSoTimeout(timeout);
        rceivePacket = new DatagramPacket(receiveBuffer, bufferSize); //https://developer.android.com/reference/java/net/DatagramPacket.html
    }

    public void send (final String input) throws UnknownHostException {

        byte data[] = input.getBytes();
        final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(Host), this.SentPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBytes(final byte data[]) throws UnknownHostException {

        final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(Host), this.SentPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void error(IOException e) {
        Log.d("error", e.toString());
        // TODO
    }

    public byte[] receive() throws IOException {
        socket.receive(rceivePacket);
        return rceivePacket.getData();
    }

    public DatagramPacket receivePkt() throws IOException {
        socket.receive(rceivePacket);
        return rceivePacket;
    }

    public byte[] receive_() throws IOException {
        socket.receive(rceivePacket);
        byte b[] = new byte[rceivePacket.getLength()];
        System.arraycopy(rceivePacket.getData(), rceivePacket.getOffset(), b, 0, rceivePacket.getLength());
        return b;
    }
}

