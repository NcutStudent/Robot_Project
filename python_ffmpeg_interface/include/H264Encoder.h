#pragma once
extern "C" {
#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>
#include <libavutil/channel_layout.h>
#include <libavutil/common.h>
#include <libavutil/imgutils.h>
#include <libavutil/mathematics.h>
#include <libavutil/samplefmt.h>
#include <libswscale/swscale.h>
#include <libavformat/avformat.h>
}

#include <opencv2/opencv.hpp>
#include <iostream>
#include <vector>

class H264Encoder {
public:
    H264Encoder(int height, int width);
    std::vector<uint8_t> encode(cv::Mat &mat);
    ~H264Encoder();
private:
    AVCodecContext *avCtx = NULL;
    AVCodec *encodec = NULL;
    AVPacket *pkt = NULL;
    struct SwsContext *bgr_2_encode_img_ctx = NULL;
    AVFrame *encode_frame = NULL;
    std::vector<uint8_t> encode_frame_buffer;
    int frame_id = 0;
    bool isOpen = false;
};
