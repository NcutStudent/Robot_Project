#include <boost/python.hpp>
#include <boost/python/numpy.hpp>

#include <include/H264ImgProcess.h>
#include <include/FaceDetected_python.h>


BOOST_PYTHON_MODULE(libpython_interface) 
{
    using namespace boost::python;
    boost::python::numpy::initialize();
    class_<H264ImgProcess>("H264ImgProcess", init<int, int>())
        .def("encodeImage",     &H264ImgProcess::encodeImage)
        .def("pushImageData",   &H264ImgProcess::pushImageData)
        .def("getImage",        &H264ImgProcess::getImage)
    ;
    
    class_<FaceDetected>("FaceDetected")
        .def("detectedImage",   &FaceDetected::detectedImage)
        .def("compare",         &FaceDetected::compare)
    ;

    class_<matrix<float,0,1>>("dlib.matrix");
}
