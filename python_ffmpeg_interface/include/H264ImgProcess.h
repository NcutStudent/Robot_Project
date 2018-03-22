#include <mutex>

#include <opencv2/opencv.hpp>
#include <boost/python.hpp>
#include <boost/python/numpy.hpp>

#include "include/H264Encoder.h"
#include "include/H264Decoder.h"


class H264ImgProcess {
public:
    H264ImgProcess(int height, int width);
    H264ImgProcess(const H264ImgProcess& object);

    boost::python::numpy::ndarray encodeImage (boost::python::numpy::ndarray &matData);
    void                          pushImageData (boost::python::numpy::ndarray &imgData);
    boost::python::numpy::ndarray getImage ();

    const int height;
    const int width;
private:
    std::mutex encode_locker;
    std::mutex decode_locker;

    H264Encoder encoder;
    H264Decoder decoder;

    std::list<std::vector<uint8_t>> imgDataList;
};

