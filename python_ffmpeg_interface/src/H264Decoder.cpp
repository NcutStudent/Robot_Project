#include "include/H264Decoder.h"

using namespace std;

H264Decoder::H264Decoder(int height, int width)
{
    av_register_all();
	if (!decodec) {
		decodec = avcodec_find_decoder(AV_CODEC_ID_H264);
		if (!decodec) {
			return;
		}
	}
	if (!deavCtx) {
		deavCtx = avcodec_alloc_context3(decodec);
		if (!deavCtx) {
			return;
		}
	}
	int ret = avcodec_open2(deavCtx, decodec, NULL);
	if (ret < 0) {
		return;
	}
	if (!decode_img_2_bgr_ctx) {
		decode_img_2_bgr_ctx = sws_getContext(
			width,
			height,
			AV_PIX_FMT_YUV420P,
			width,
			height,
			AV_PIX_FMT_BGR24,
			SWS_BICUBIC,
			NULL, NULL, NULL);
		if (!decode_img_2_bgr_ctx) {
			return;
		}
	}
	if (!pkt) {
		pkt = av_packet_alloc();
		if (!pkt) {
			return;
		}
	}
	if (!parser) {
		parser = av_parser_init(decodec->id);
		if (!parser) {
			return;
		}
	}
	if (!decode_frame) {
		decode_frame = av_frame_alloc();
		if (!decode_frame) {
			return;
		}
		decode_frame_buffer.resize(avpicture_get_size(AV_PIX_FMT_YUV420P, width, height));
		avpicture_fill(reinterpret_cast<AVPicture*>(decode_frame), decode_frame_buffer.data(),
			AV_PIX_FMT_YUV420P, width, height);

		decode_frame->width = width;
		decode_frame->height = height;
		decode_frame->format = static_cast<int>(AV_PIX_FMT_YUV420P);
	}

	if (!brg_frame) {
		brg_frame = av_frame_alloc();
		if (!brg_frame) {
			return;
		}
		brg_frame_buffer.resize(avpicture_get_size(AV_PIX_FMT_BGR24, width, height));
		avpicture_fill(reinterpret_cast<AVPicture*>(brg_frame), brg_frame_buffer.data(),
			AV_PIX_FMT_BGR24, width, height);

		brg_frame->width = width;
		brg_frame->height = height;
		brg_frame->format = static_cast<int>(AV_PIX_FMT_BGR24);
	}
	isOpen = true;
}

void H264Decoder::decode(vector<uint8_t> str_data, std::list<vector<uint8_t>> &frame_list)
{
	uint8_t * data = str_data.data();
	int data_size = str_data.size();
    decode(data, data_size, frame_list);
}

void H264Decoder::decode(uint8_t *data, int data_size, std::list<vector<uint8_t>> &frame_list) 
{
	while (data_size > 0) {
		int ret = av_parser_parse2(parser, deavCtx, &pkt->data, &pkt->size,
			data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
		if (ret < 0) {
			return;
		}

		if (pkt->size)
			decode(deavCtx, pkt, frame_list);

		data += ret;
		data_size -= ret;
	}
}

void H264Decoder::decode(AVCodecContext *dec_ctx, AVPacket *pkt, std::list<vector<uint8_t>> &frame_list)
{
	int ret;
	ret = avcodec_send_packet(dec_ctx, pkt);
	if (ret < 0) {

		char a[60];
		av_strerror(ret, a, 60);
		return;
	}
	while (ret >= 0) {
		ret = avcodec_receive_frame(dec_ctx, decode_frame);
		if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
			return;
		else if (ret < 0) {
			return;
		}
		if (decode_frame->format == AV_PIX_FMT_BGR24) {
			frame_list.push_back(decode_frame_buffer);
		}
		else {
			sws_scale(decode_img_2_bgr_ctx,
				(const unsigned char* const*)decode_frame->data,
				decode_frame->linesize,
				0,
				decode_frame->height,
				brg_frame->data,
				brg_frame->linesize);
			 frame_list.push_back(brg_frame_buffer);
		}
	}
}

H264Decoder::~H264Decoder() 
{
    avcodec_free_context(&deavCtx);
    // codec do not need to free
    av_packet_free(&pkt);
    av_parser_close(parser);
    av_frame_free(&decode_frame);
    av_frame_free(&brg_frame);
    sws_freeContext(decode_img_2_bgr_ctx);
}

