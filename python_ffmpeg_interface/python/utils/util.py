import cv2
import numpy

def drawRect(rect, img):
    if len(rect) != 4:
        return
    if type(img) != numpy.ndarray:
        return
    cv2.rectangle(img, (rect[2], rect[0]), (rect[3], rect[1]), (0, 0, 255), 2)

def drawText(text, point, img):
    if len(point) != 2:
        return
    cv2.putText(img, text, point, cv2.FONT_HERSHEY_SIMPLEX, 3, (0, 0, 255), 2)
