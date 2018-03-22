import ctypes
import cv2
import numpy as np

import httplib
import urllib

cap=cv2.VideoCapture(0)
http = httplib.HTTPConnection('127.0.0.1', 9999)
while(True):
    ret, frame = cap.read()
#    cv2.waitKey(30)
    gray=cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    cv2.imshow('frame', gray)
    cv2.waitKey(1)

#    cv2.waitKey(30)
#    cv2.imwrite('tmp.jpg', frame)
    
 #   cv2.waitKey(30)
 #   f={'path': 'tmp.jpg'}

#    param=urllib.urlencode(f)
#    http.request("GET", "/?"+param);
#    text=http.getresponse()
#    data=text.read()
#    print(data)
cap.release()
cv2.destroyAllWindows() 
