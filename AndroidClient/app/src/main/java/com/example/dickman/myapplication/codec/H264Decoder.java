package com.example.dickman.myapplication.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by aeon on 2018/3/21.
 */

public class H264Decoder extends  Thread {
    MediaCodec mediaCodec;
    int bufferSize = 10;
    final List<byte[]> imageByteBuffer = new LinkedList<>();
    public H264Decoder(Surface surface) throws IOException {
        final MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);

        mediaCodec = MediaCodec.createDecoderByType("video/avc");
        mediaCodec.configure(mediaFormat, surface, null, 0);
        mediaCodec.start();
        this.start();
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        for (; ; ) {
            if (imageByteBuffer.size() == 0) {
                continue;
            }
            byte[] data;
            synchronized (imageByteBuffer) {
                data = imageByteBuffer.remove(0);
            }
            int i;
            do {
                i = mediaCodec.dequeueInputBuffer(1000);
            } while (i == -1);
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(i);
            inputBuffer.clear();
            inputBuffer.put(data, 0, data.length);
            mediaCodec.queueInputBuffer(i, 0, data.length, 0, 0);

            int outIndex = mediaCodec.dequeueOutputBuffer(info, 1000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED: // fuck this
                    break;

                default:
                    mediaCodec.releaseOutputBuffer(outIndex, info.presentationTimeUs);

            }
        }
    }

    public void decodeByte(byte[] bytes) {
        synchronized (imageByteBuffer) {
            imageByteBuffer.add(bytes);
        }
    }
}
