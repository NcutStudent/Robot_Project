#include "include/H264Encoder.h"

using namespace std;
using namespace cv;

H264Encoder::H264Encoder(int height, int width)
{
    av_register_all();
	if (!encodec) {
		encodec = avcodec_find_encoder(AV_CODEC_ID_H264);
		if (!encodec) {
            cout << "Can't open encoder" << endl;
			return;
		}
	}
	if (!avCtx) {
		avCtx = avcodec_alloc_context3(encodec);
		if (!avCtx) {
			return;
		}
        // set delay to 0
        av_opt_set(avCtx->priv_data, "preset", "superfast", 0);
        av_opt_set(avCtx->priv_data, "tune", "zerolatency", 0);
		avCtx->bit_rate = 400000;
		avCtx->height = height;
		avCtx->width = width;
		avCtx->time_base.num = 1; avCtx->time_base.den = 25;
		avCtx->framerate.num = 15; avCtx->framerate.den = 1;
		avCtx->gop_size = 10;
		avCtx->max_b_frames = 1;
		avCtx->pix_fmt = AV_PIX_FMT_YUV420P;
	}
	int ret = avcodec_open2(avCtx, encodec, NULL);
	if (ret < 0) {
		return;
	}
	if (!bgr_2_encode_img_ctx) {
		bgr_2_encode_img_ctx = sws_getContext(
			width,
			height,
			AV_PIX_FMT_BGR24,
			width,
			height,
			AV_PIX_FMT_YUV420P,
			SWS_BICUBIC,
			NULL, NULL, NULL);
		if (!bgr_2_encode_img_ctx) {
			return;
		}
	}
	if (!pkt) {
		pkt = av_packet_alloc();
		if (!pkt) {
			return;
		}
	}

	if (!encode_frame) {
		encode_frame = av_frame_alloc();
		if (!encode_frame) {
			return;
		}
		encode_frame_buffer.resize(avpicture_get_size(AV_PIX_FMT_YUV420P, width, height));
		avpicture_fill(reinterpret_cast<AVPicture*>(encode_frame), encode_frame_buffer.data(),
			AV_PIX_FMT_YUV420P, width, height);

		encode_frame->width = width;
		encode_frame->height = height;
		encode_frame->format = static_cast<int>(AV_PIX_FMT_YUV420P);
	}

	isOpen = true;
}

vector<uint8_t> H264Encoder::encode(cv::Mat & mat)
{
	if(isOpen){
		const int stride[] = { static_cast<int>(mat.step[0]) };
		sws_scale(bgr_2_encode_img_ctx, &mat.data, stride, 0, mat.rows,
			encode_frame->data, encode_frame->linesize);

		encode_frame->pts = frame_id++;

		int ret = avcodec_send_frame(avCtx, encode_frame);
		ret = avcodec_receive_packet(avCtx, pkt);

		if (ret < 0 || pkt->size <= 0) {
            char a[60];
            av_strerror(ret, a, 60);
            cout << a << endl;
			return vector<uint8_t>();
		}
		return vector<uint8_t>(pkt->data, pkt->data + pkt->size);
	}
	return vector<uint8_t>();
}

H264Encoder::~H264Encoder() 
{
    avcodec_free_context(&avCtx);
    // codec do not need to free
    av_packet_free(&pkt);
    sws_freeContext(bgr_2_encode_img_ctx);
    av_frame_free(&encode_frame);
}
