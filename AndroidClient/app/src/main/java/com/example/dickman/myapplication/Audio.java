package com.example.dickman.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.dickman.myapplication.network.UDP_Request;

import java.io.IOException;
import java.net.DatagramSocket;

class ThreadKeepRunning extends Thread{
    private boolean stopped = false;
    public void close() {
        stopped = true;
    }
}

public class Audio extends Thread
{
    private final String RaspberryKey = "Raspberry";

    private int client_frame_size = 20480; // dont cnahge it?????????
    private ThreadKeepRunning recieveAudio;
    private boolean stopped = false;
    private String token;
    private int rate = 44100;
    private UDP_Request udp_connect ;
    private AudioRecord recorder = null;
    private AudioTrack track = null;

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and start it
     */
    public Audio(DatagramSocket socket, String Host, int SentPort, int timeoutAudio, String token)
    {
        try {
            udp_connect = new UDP_Request (socket, Host, SentPort, timeoutAudio, client_frame_size);//??UDP_Request??????socket?MainActivity????????IP????Port?timeout??????
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.token = token;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);//????????????

        recieveAudio = new ThreadKeepRunning() {
            @Override
            public void run() {
                super.run();
                while(!stopped){//????????
                    try {
                        if(track != null && track.getState() == AudioTrack.STATE_INITIALIZED) {//???AudioTrack??????track ?????????
                            byte[] recieve_data = udp_connect.receive();//????????????
                            if(recieve_data != null)//???????????
                                track.write(recieve_data, 0, recieve_data.length);//????? track.write(???????, ??0, ????recieve_data.length)
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        start();
        recieveAudio.start();//???????
    }

    @Override
    public void run()
    {
        try
        {
            //?????????????? getMinBufferSize (???[44100Hz???????????????????]??????????????)
            int N = AudioRecord.getMinBufferSize(rate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);

            //??AudioRecord (???????[44100Hz???????????????????]?????????????????????????????????)
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10);

            //??AudioTrack (????STREAM_MUSIC[??????????????]????[44100Hz???????????????????]???????????????????????????????????????????MODE_STREAM[?????Java????????????])
            track = new AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 10, AudioTrack.MODE_STREAM);

            recorder.startRecording();//?AudioRecord??????
            track.play();//??????

            byte[] token = this.token.getBytes();
            byte[] RaspberryKey = this.RaspberryKey.getBytes();
            int offset = token.length + RaspberryKey.length + 2;//??????token?key????????????
            final byte[] send_buffer = new byte[client_frame_size + offset];//??????????
            System.arraycopy(token, 0, send_buffer, 0, token.length);//????????token
            send_buffer[token.length] = ' ';//????
            System.arraycopy(RaspberryKey, 0, send_buffer, token.length + 1, RaspberryKey.length);//????key
            send_buffer[offset - 1] = ' ';//???????????
            while(!stopped)  //Sent data frame: token <key> <data>????????
            {
                recorder.read(send_buffer, offset, send_buffer.length - offset);//?????key?????
                udp_connect.sendBytes(send_buffer);//??udp????

            }
            recieveAudio.close();
        }
        catch(Throwable x)
        {
            Log.w("Audio", "Error reading voice audio", x);
        }
        /*
         * Frees the thread's resources after the loop completes so that it can be run again
         */
        finally//?????
        {
            if(recorder != null) {//???????????????
                recorder.stop();
                recorder.release();
            }
            if(track != null) {//???????????????
                track.stop();
                track.release();
            }
        }
    }

    /**
     * Called from outside of the thread in order to stop the recording/playback loop
     */
    public void close()//????
    {
        recieveAudio.close();
        stopped = true;
    }

}