import cv2
import os
import libpython_interface
import utils.util as util

tx2Camera = 'nvcamerasrc ! video/x-raw(memory:NVMM), width=(int)640, height=(int)480, format=(string)I420, framerate=(fraction)30/1 ! nvvidconv  ! video/x-raw, format=(string)I420 ! videoconvert ! video/x-raw, format=(string)BGR ! appsink'

cap = cv2.VideoCapture(tx2Camera)
res, frame = cap.read()

faceDetected = libpython_interface.FaceDetected()

file_path = '../test_pic/'
files = os.listdir(file_path)

face_list = []

for f in files:
    if os.path.isdir(f):
        continue;
    filename, file_extension = os.path.splitext(f)
    img = cv2.imread(file_path + f)
    if img is None:
        continue
    faces = faceDetected.detectedImage(img, len(img), len(img[0]))
    if len(faces) > 1:
        print("Warning, one pic one face")
        print("file " + f)
        continue
    face_list.append((faces[0][0], filename))
    print("add " + filename)

def getXY(rect):
    x = min(rect[0], rect[1])
    y = min(rect[2], rect[3])
    return (y, x)

while True:
    frame = cap.read()[1]
    faces = faceDetected.detectedImage(frame, len(frame), len(frame[0]))
    for face, rect in faces:
        util.drawRect(rect, frame)
        for i, name in face_list:
            s = faceDetected.compare(face, i)
            if s < 0.32:
                util.drawText(name, getXY(rect), frame)
                break;
    cv2.imshow('frame', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break
