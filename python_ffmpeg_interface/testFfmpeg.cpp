#pragma
extern "C" {
#include <libavutil/opt.h>
#include <libavcodec/avcodec.h>
#include <libavutil/channel_layout.h>
#include <libavutil/common.h>
#include <libavutil/imgutils.h>
#include <libavutil/mathematics.h>
#include <libavutil/samplefmt.h>
#include <libswscale/swscale.h>
#include <libavdevice/avdevice.h>
#include <libavformat/avformat.h>
}

#include <opencv2/opencv.hpp>
#include <iostream>
#include "include/H264Decoder.h"
#include "include/H264Encoder.h"

using namespace std;
using namespace cv;

void show_dshow_device() {
	AVFormatContext *pFormatCtx = avformat_alloc_context();
	AVDictionary* options = NULL;
	av_dict_set(&options, "list_devices", "true", 0);
	AVInputFormat *iformat = av_find_input_format("dshow");
	printf("Device Info=============\n");
	avformat_open_input(&pFormatCtx, "video=dummy", iformat, &options);
	printf("========================\n");
}

int main() {
	av_register_all();
	//avdevice_register_all();
	//show_dshow_device();

	const int dst_width = 640;
	const int dst_height = 480;

	VideoCapture cap(1);
    if(!cap.isOpened()) {
        cout << "No Such Camera" << endl;
        return -1;
    }
    
	std::vector<uint8_t> imgbuf(dst_height * dst_width * 3 + 16);
	cv::Mat frame(dst_height, dst_width, CV_8UC3, imgbuf.data(), dst_width * 3);

	namedWindow("frame");

	H264Encoder e(dst_height, dst_width);
	H264Decoder h(dst_height, dst_width);

	list<vector<uint8_t>> res;

	for (;;) {
		if (cap.read(frame)) {
			if (frame.empty())
				continue;
			vector<uint8_t> c = e.encode(frame);

			if (!(c.size() > 0)) {
				continue;
			}

            cout << c.size() << endl;
			h.decode(c, res);

			if (res.size() > 0) {
 				cv::Mat img(frame.rows, frame.cols, CV_8UC3, res.front().data()); //AVFrame to Mat
				imshow("frame", img);
				waitKey(1);
				res.pop_front();
			}
		}
	}
	return 0;
}
