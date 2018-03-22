#include <include/H264ImgProcess.h>

#include <iostream>

#include <mutex>

namespace python = boost::python;
namespace numpy = boost::python::numpy;

using namespace std;

H264ImgProcess::H264ImgProcess(int height, int width) : encoder(height, width),
                                                        decoder(height, width),
                                                        height(height),
                                                        width(width) 
                                                        {}

H264ImgProcess::H264ImgProcess(const H264ImgProcess &process) : encoder(process.height, process.width),
                                                                decoder(process.height, process.width),
                                                                height(process.height),
                                                                width(process.width)
                                                                {}

numpy::ndarray H264ImgProcess::encodeImage (numpy::ndarray &matData) {

    cv::Mat imgBuf(height, width, CV_8UC3, matData.get_data());

    vector<uint8_t> res;

    encode_locker.lock();

    res = encoder.encode(imgBuf);

    Py_intptr_t shape[1] = { static_cast<Py_intptr_t>(res.size()) };
    numpy::ndarray result = numpy::zeros(1, shape, numpy::dtype::get_builtin<uint8_t>());
    std::copy(res.begin(), res.end(), result.get_data());

    encode_locker.unlock();

    return result;
}

void H264ImgProcess::pushImageData(numpy::ndarray &imgData)
{
    uint8_t *data = (uint8_t *)imgData.get_data();
    int data_size= imgData.shape(0);

    decode_locker.lock();

    decoder.decode(data, data_size, imgDataList);

    decode_locker.unlock();
}

numpy::ndarray H264ImgProcess::getImage () {

    if(imgDataList.size() > 0) {
        decode_locker.lock();
        if(imgDataList.size() > 0){
            auto data = imgDataList.front();
            
            Py_intptr_t shape[] = { height, width, 3};
            numpy::ndarray result = numpy::zeros(3, shape, numpy::dtype::get_builtin<uint8_t>());
            std::copy(data.begin(), data.end(), result.get_data());

            imgDataList.pop_front();
         
            decode_locker.unlock(); // notice this
            return result;
        }
        decode_locker.unlock();
    }
    Py_intptr_t shape[1] = { 0 };
    numpy::ndarray result = numpy::zeros(1, shape, numpy::dtype::get_builtin<uint8_t>());
    return result;
}
