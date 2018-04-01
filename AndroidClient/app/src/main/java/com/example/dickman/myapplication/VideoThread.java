package com.example.dickman.myapplication;

import android.hardware.camera2.CameraDevice;
import android.view.Surface;

import com.example.dickman.myapplication.codec.H264Decoder;
import com.example.dickman.myapplication.codec.H264Encoder;
import com.example.dickman.myapplication.network.SlidingWindow;
import com.example.dickman.myapplication.network.TCP_Connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by aeon on 2018/3/22.
 */

public class VideoThread extends Thread {
    H264Decoder h264Decoder;
    H264Encoder h264Encoder;
    SlidingWindow slidingWindow;
    String token;
    String raspberryKey = "VideoRaspberry";
    String phonekey = "VideoRaspberry";
    boolean isRunning = false;
    public VideoThread(TCP_Connect tcp_connect, CameraDevice device, Surface outputSurface, int width, int height) {
        this.token = tcp_connect.getToken();
        final DatagramSocket socket = tcp_connect.getUdpSocket(phonekey);
        InetAddress ip = null;
        int port = 0;
        do {
            String ip_port = tcp_connect.getSlidingIp_Port(raspberryKey);
            String data[] = ip_port.split(" ");
            if(data[1].equals("0")) {
                continue;
            }
            port = Integer.valueOf(data[1]);
            try {
                ip = InetAddress.getByName(data[0]);
            } catch (UnknownHostException e) {
                port = 0;
            }
        } while(port == 0);
        try {
            h264Encoder = new H264Encoder(device, width, height, token.length() + phonekey.length() + raspberryKey.length() + 3);
            h264Decoder = new H264Decoder(outputSurface);
            slidingWindow = new SlidingWindow(token + " " + phonekey + " " + raspberryKey + " ", socket, (byte)10, 100, ip, port);

            byte[] key = (phonekey + " " + raspberryKey).getBytes();
            byte[] token = this.token.getBytes();
            byte[] data = new byte[key.length + token.length + 3];
            System.arraycopy(token, 0, data, 0, token.length);
            data[token.length] = ' ';
            System.arraycopy(key, 0, data, token.length + 1, key.length);
            data[token.length + key.length + 1] = ' ';
            data[token.length + key.length + 2] = ~0;
            final DatagramPacket pk = new DatagramPacket(data, data.length, ip, port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.send(pk);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;isRunning;) {
                    byte[] data = slidingWindow.getData();
                    if(data != null) {
                        h264Decoder.decodeByte(data);
                    }
                }
            }
        }).start();
        this.start();
        isRunning = true;
    }

    @Override
    public void run() {
        for(;isRunning;) {
            byte[] data = h264Encoder.getEncodeedImage();
            if(data == null)
                continue;
            byte[] key = (phonekey + " " + raspberryKey).getBytes();
            byte[] token = this.token.getBytes();
            System.arraycopy(token, 0, data, 0, token.length);
            data[token.length] = ' ';
            System.arraycopy(key, 0, data, token.length + 1, key.length);
            data[token.length + key.length + 1] = ' ';
            try {
                slidingWindow.sendData(data, data.length - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    synchronized void stopRunning() {
        isRunning = false;
        slidingWindow.stopRunning();
    }
}
