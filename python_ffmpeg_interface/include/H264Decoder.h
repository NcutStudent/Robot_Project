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

#include <iostream>
#include <list>
#include <vector>

class H264Decoder {
public:
    H264Decoder(int height, int width);
    void decode(std::vector<uint8_t> data, std::list<std::vector<uint8_t>> &frame_list);
    void decode(uint8_t *data, int data_size, std::list<std::vector<uint8_t>> &frame_list);
    void decode(AVCodecContext * dec_ctx, AVPacket * pktM, std::list<std::vector<uint8_t>> &frame_list);
    ~H264Decoder();
private:

	AVCodecContext *deavCtx = NULL;
	AVCodec *decodec = NULL;
	AVPacket *pkt = NULL;
	AVCodecParserContext *parser = NULL;
	AVFrame *decode_frame = NULL;
	AVFrame *brg_frame = NULL;
	struct SwsContext *decode_img_2_bgr_ctx = NULL;

	std::vector<uint8_t> decode_frame_buffer;
	std::vector<uint8_t> brg_frame_buffer;
	std::list<std::string> frame_list;
	bool isOpen = false;
};
