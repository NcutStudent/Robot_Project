#include <include/FaceDetected.h>

FaceDetected::FaceDetected()
{
    detector = get_frontal_face_detector();
    deserialize("shape_predictor_5_face_landmarks.dat") >> sp;
    deserialize("dlib_face_recognition_resnet_model_v1.dat") >> net;
}

FaceDescriptors FaceDetected::detectedImage(const Mat &mat) {

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
        res.push_back(make_pair(face_descriptors[i], face_rectangle[i]));
    }
    return res;
}

float FaceDetected::compare(matrix<float,0,1> &face1, matrix<float,0,1> &face2) {
    return length(face1 - face2);
}

