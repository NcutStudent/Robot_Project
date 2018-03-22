import cv2
import sys

if len(sys.argv) != 2:
    print("use command like this : ")
    print("python capPicture.py <your file name here>")
    exit()

tx2Camera = 'nvcamerasrc ! video/x-raw(memory:NVMM), width=(int)640, height=(int)480, format=(string)I420, framerate=(fraction)30/1 ! nvvidconv  ! video/x-raw, format=(string)I420 ! videoconvert ! video/x-raw, format=(string)BGR ! appsink'

cap = cv2.VideoCapture(tx2Camera)

while True:
    frame = cap.read()[1]
    cv2.imshow('frame', frame)
    cv2.imwrite('../test_pic/' + sys.argv[1], frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

print('your pic store at ../test_pic/' + sys.argv[1])
cap.release()
cv2.destroyAllWindows()
