#include <include/FaceDetected_python.h>

FaceDetected::FaceDetected()
{
    detector = get_frontal_face_detector();
    deserialize("../shape_predictor_5_face_landmarks.dat") >> sp;
    deserialize("../dlib_face_recognition_resnet_model_v1.dat") >> net;
}

FaceDescriptors FaceDetected::detectedImage(const boost::python::numpy::ndarray &data, 
                                                int height, 
                                                int width) {

    cv::Mat mat(height, width, CV_8UC3, data.get_data());
    cv_image<bgr_pixel> img(mat);
    std::vector<matrix<rgb_pixel>> faces;
    FaceDescriptors res;
    std::vector<dlib::rectangle> face_rectangle = detector(img);
    for (auto face : face_rectangle)
    {
        auto shape = sp(img, face);
        matrix<rgb_pixel> face_chip;
        extract_image_chip(img, get_face_chip_details(shape,150,0.25), face_chip);
        faces.push_back(move(face_chip));
    }

    if (faces.size() == 0)
    {
        return FaceDescriptors();
    }

    std::vector<matrix<float,0,1>> face_descriptors = net(faces);
    for(int i = 0; i < face_descriptors.size(); ++i) {
        auto rectangle = face_rectangle[i];
        boost::python::tuple t_rectangle = 
            boost::python::make_tuple(
                rectangle.top(), 
                rectangle.bottom(),
                rectangle.left(),
                rectangle.right()
            );

        res.append(boost::python::make_tuple(face_descriptors[i], t_rectangle));
    }
    return res;
}

float FaceDetected::compare(matrix<float,0,1> &face1, matrix<float,0,1> &face2) {
    return length(face1 - face2);
}

