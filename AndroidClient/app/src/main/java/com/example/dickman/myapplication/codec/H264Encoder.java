package com.example.dickman.myapplication.codec;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by aeon on 2018/3/21.
 */

public class H264Encoder extends MediaCodec.Callback{
    private MediaCodec mediaCodec;
    private Surface surface;
    private CameraDevice cameraDevice;
    private int offset;
    private final List<byte[]> byteBuffers = new LinkedList<>();
    boolean isRunning = false;
    private CameraCaptureSession session;

    class MyCameraCaptureSessionCallBack extends CameraCaptureSession.StateCallback{

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {

            try {
                CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                request.set(CaptureRequest.JPEG_ORIENTATION, 270);
                request.addTarget(surface);

                CaptureRequest captureRequest = request.build();
                session.setRepeatingRequest(captureRequest, null, null);
                H264Encoder.this.session = session;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    }

    MyCameraCaptureSessionCallBack cameraCallback = new MyCameraCaptureSessionCallBack();

    public H264Encoder(final CameraDevice cameraDevice, int width, int height, int offset) throws IOException {
        this.cameraDevice = cameraDevice;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        this.offset = offset;

        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 400000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);

        mediaCodec = MediaCodec.createEncoderByType("video/avc");
        mediaCodec.setCallback(this);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();
        isRunning = true;


        try {
            cameraDevice.createCaptureSession(Collections.singletonList(surface), cameraCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
        ByteBuffer byteBuffer =mediaCodec.getOutputBuffer(index);
        if(byteBuffer == null) {
            mediaCodec.releaseOutputBuffer(index, false);
            return;
        }
        byte[] bytes = new byte[byteBuffer.remaining() + offset + 1];
        byteBuffer.get(bytes, offset, byteBuffer.remaining());

        synchronized (byteBuffers) {
            byteBuffers.add(bytes);
        }

        while(byteBuffers.size() > 20) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaCodec.releaseOutputBuffer(index, false);
    }

    @Override
    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

    }

    public byte[] getEncodeedImage() {
        synchronized (byteBuffers) {
            if (byteBuffers.size() != 0)
                return byteBuffers.remove(0);
            return null;
        }
    }
    synchronized public void stopEncoding() {
        mediaCodec.stop();
        if(session != null) {
            session.close();
            session = null;
        }
    }
}
