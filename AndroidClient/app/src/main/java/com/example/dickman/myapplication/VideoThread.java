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
    String phonekey = "VideoPhone";
    boolean isRunning = false;
    //VideoThread(tcp_connect的連接設定，cameraDevice啟動相機功能，surfaceView.getHolder().getSurface()將接收到的資料畫面渲染到surfaceView上，設定畫面大、小)
    public VideoThread(TCP_Connect tcp_connect, CameraDevice device, Surface outputSurface, int width, int height) {
        this.token = tcp_connect.getToken();//從tcp_connect設定需要的token
        final DatagramSocket socket = tcp_connect.getUdpSocket(phonekey);//將需要的金鑰傳給tcp_connect.getUdpSocket
        InetAddress ip = null;
        int port = 0;
        do {
            String ip_port = tcp_connect.getSlidingIp_Port(raspberryKey);//將需要的金鑰傳給tcp_connect.getSlidingIp_Port
            String data[] = ip_port.split(" ");//將接收到的資料透過切割分成IP跟port
            if(data[1].equals("0")) {
                continue;
            }//若port為0則繼續往下做，並不跳出while
            port = Integer.valueOf(data[1]);//轉換port變成數值
            try {
                ip = InetAddress.getByName(data[0]);//藉由InetAddress.getByName取得ip名稱
            } catch (UnknownHostException e) {
                port = 0;
            }
        } while(port == 0);//只要為0，就表示沒收到port，所以繼續收
        try {
            //H264Encoder建構編碼的程序，將H264Encoder(相機拍攝畫面，畫面大小，跟起始位置[從金鑰跟token以及中間的空白之後算起])
            //就是說這邊為了速度問題而把前面空下來省下複製byte array 的時間
            h264Encoder = new H264Encoder(device, width, height, token.length() + phonekey.length() + raspberryKey.length() + 3);
            h264Decoder = new H264Decoder(outputSurface);

            /*為了讓傳輸速度更快而且更加完整，採用滑動式視窗(slidingWindow)做法
                            SlidingWindow (標頭[將token跟金鑰放入]，廣播用socket資料，視窗大小，timeouti，接收端IP，接收端port)*/
            slidingWindow = new SlidingWindow(token + " " + phonekey + " " + raspberryKey + " ", socket, (byte)10, 100, ip, port);

            byte[] key = (phonekey + " " + raspberryKey).getBytes();
            byte[] token = this.token.getBytes();
            byte[] data = new byte[key.length + token.length + 3];//建立一個data陣列，其大小的判斷則是將金鑰、token跟3格空白算進去
            System.arraycopy(token, 0, data, 0, token.length);//複製陣列，先把token加入
            data[token.length] = ' ';//加入空白
            System.arraycopy(key, 0, data, token.length + 1, key.length);//從token之後再加入金鑰
            data[token.length + key.length + 1] = ' ';
            data[token.length + key.length + 2] = ~0;
            final DatagramPacket pk = new DatagramPacket(data, data.length, ip, port);//將要傳送的資料利用DatagramPacket包起
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.send(pk);//傳送pk資料
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
                    byte[] data = slidingWindow.getData();//呼叫slidingWindow的得到資料，確定資料是否從接收端獲得
                    if(data != null) {
                        h264Decoder.decodeByte(data);//如果資料不是null，就存放到h264Decoder的imageByteBuffer裡面
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
            byte[] data = h264Encoder.getEncodeedImage();//從h264Encoder.getEncodeedImage拿圖片出來
            if(data == null)
                continue;//如果data沒東西，就重複拿取，直到有值為止
            byte[] key = (phonekey + " " + raspberryKey).getBytes();
            byte[] token = this.token.getBytes();
            System.arraycopy(token, 0, data, 0, token.length);//複製陣列，先把token加入
            data[token.length] = ' ';
            System.arraycopy(key, 0, data, token.length + 1, key.length);//從token之後加入金鑰
            data[token.length + key.length + 1] = ' ';
            try {
                slidingWindow.sendData(data, data.length - 1);//利用slidingWindow傳送資料，slidingWindow (資料，資料長度-1[扣除空白])
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    synchronized void stopRunning() {//設定滑動視窗法只能在線程實作一個
        isRunning = false;
        slidingWindow.stopRunning();
    }
}
