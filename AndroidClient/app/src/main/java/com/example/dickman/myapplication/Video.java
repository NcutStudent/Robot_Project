package com.example.dickman.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by DickMan on 2017/12/11.
 */
class CombineSocket {
    UDP_Request udp_connect;
    List<byte[]> reciveList = new LinkedList<>();
    public CombineSocket(DatagramSocket socket, String Host, int SentPort, int timeout) throws IOException {
        udp_connect = new UDP_Request (socket, Host, SentPort, timeout, 65400);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(;;) {
                        if (reciveList.size() < 128) {
                            reciveList.add(udp_connect.receive());
                        } else {
                            udp_connect.receive_();
                        }
                        Thread.sleep(1);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public byte[] receive() { //TODO rewrite this
        byte[] ans = null;
        if(reciveList.size() < 2){
            return null;
        }
        if(true){
            ans = new byte[reciveList.get(0).length + reciveList.get(1).length - 2];
             System.arraycopy(reciveList.get(0), 1, ans, 0, reciveList.get(0).length - 1);
            System.arraycopy(reciveList.get(1), 1, ans, reciveList.get(0).length - 1, reciveList.get(1).length - 1);
            reciveList.remove(0);
            reciveList.remove(0);
        } else {
            ans = new byte[reciveList.get(0).length - 1];
            System.arraycopy(reciveList.get(0), 1, ans, 0, reciveList.get(0).length - 1);
            reciveList.remove(0);
        }
        return ans;
    }

    public void send(final byte data[]) { //TODO rewrite protocol

    }
}
public class Video {
    CombineSocket combineSocket;
    List<Bitmap> frameList = new LinkedList<>();
    public Video (DatagramSocket socket, String Host, int SentPort, int timeoutVideo)
    {
        try {
            combineSocket = new CombineSocket(socket, Host, SentPort, timeoutVideo);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (; ; ) {
                        byte imageByte[] = combineSocket.receive();
                        while (imageByte == null) {
                            try {
                                Thread.sleep(1);
                                imageByte = combineSocket.receive();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

//                        Mat mat = Imgcodecs.imdecode(new MatOfByte(imageByte), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//                        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                        //Utils.matToBitmap(mat, bmp);
                        Bitmap bmp = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        if (bmp != null) {
                            frameList.add(bmp);
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getFrame() {
        if(frameList.size() == 0)
            return null;
        Bitmap bmp = frameList.get(0);
        frameList.remove(0);
        return bmp;
    }
}
