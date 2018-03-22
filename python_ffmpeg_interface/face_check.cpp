#include <dlib/gui_widgets.h>
#include <dlib/clustering.h>
#include <dlib/string.h>
#include <dlib/dnn.h>
#include <dlib/image_io.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <iostream>
#include <fstream>
#include <string>

using namespace dlib;
using namespace std;

class FaceDetected{
public:
    FaceDetected()
    {
        detector = get_frontal_face_detector();
        deserialize("shape_predictor_68_face_landmarks.dat") >> sp;
        deserialize("dlib_face_recognition_resnet_model_v1.dat") >> net;
    }

    matrix<float,0,1> detectedImage(const std::string &path) {

        matrix<rgb_pixel> img;
        load_image(img, path);
        std::vector<matrix<rgb_pixel>> faces;
        for (auto face : detector(img))
        {
            auto shape = sp(img, face);
            matrix<rgb_pixel> face_chip;
            extract_image_chip(img, get_face_chip_details(shape,150,0.25), face_chip);
            faces.push_back(move(face_chip));
        }
 
        if (faces.size() == 0)
        {
            return matrix<float,0,1>();
        }
 
        std::vector<matrix<float,0,1>> face_descriptors = net(faces);
        return face_descriptors[0];
    }
    
private:
    frontal_face_detector detector;
    shape_predictor sp;
    anet_type net;
};

extern "C" {
    FaceDetected detected;
    void compare(matrix<float,0,1> face1, matrix<float,0,1> face2) {
        std::ofstream file("tmp.txt");
        float score=length(face1 - face2);
        score *= 100;
        file << int(score);
    }
    matrix<float,0,1> *detectedImage() {
        std::ifstream file("tmp.txt");
        std::string line;
        std::getline(file, line);
        std::cout << line << std::endl;
        auto tmp=detected.detectedImage(line);
        auto tmp1=new decltype(tmp);
        *tmp1=tmp;
        return tmp1;
    }
}
