import cv2
import libpython_interface
import utils.util as util

tx2Camera = 'nvcamerasrc ! video/x-raw(memory:NVMM), width=(int)640, height=(int)480, format=(string)I420, framerate=(fraction)30/1 ! nvvidconv  ! video/x-raw, format=(string)I420 ! videoconvert ! video/x-raw, format=(string)BGR ! appsink'

cap = cv2.VideoCapture(tx2Camera)

res, frame = cap.read()

if not res:
    print("Can't open camera")
    exit(-1)

p = libpython_interface.H264ImgProcess(len(frame), len(frame[0]))
f = libpython_interface.FaceDetected()

d = f.detectedImage(frame, len(frame), len(frame[0]))

while True:
    frame = cap.read()[1]
    faceList = f.detectedImage(frame, len(frame), len(frame[0]))

    for face, rect in faceList:
        util.drawRect(rect, frame)

    data = p.encodeImage(frame)
    if len(data) == 0:
        continue
    print(len(data))
    p.pushImageData(data)
    frame = p.getImage()
    if len(frame) == 0:
        continue
    cv2.imshow('frame', frame)
    c = cv2.waitKey(1)
