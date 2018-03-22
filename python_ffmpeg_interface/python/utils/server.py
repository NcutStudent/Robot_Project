from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urlparse
import cv2
import ctypes
from numpy.ctypeslib import ndpointer
lib = ctypes.CDLL('./f.so')
nameList=[]
faceList=[]
#lib.detection.restype = ndpointer(dtype=ctypes.c_int, shape=(7,))
#lib.classification.restype = ctypes.c_char_p
def getFace(lib, path):
    f=open('tmp.txt', 'w')
    f.write(path)
    f.close()
    return lib.detectedImage()

def getScore():
    f=open('tmp.txt', 'r')
    i=f.read()
    return int(i)

class handle(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        print('get')
        self.send_header('Content-type','text-html')
        self.end_headers()
        parsed = urlparse.urlparse(self.path)
        listPar=urlparse.parse_qs(parsed.query)
        if "name" in listPar:
            nameList.append(listPar['name'][0])
            faceList.append(getFace(lib, listPar['path'][0]));
            print('get file path ' + listPar['path'][0] + ' person name ' + listPar['name'][0])
        else:
            if len(nameList) is 0:
                self.wfile.write("no match")
                print("no match")
                return
            else:
                face=getFace(lib, listPar['path'][0]);
                for i in range(0, len(faceList)):
                    lib.compare(face, faceList[i])
                    score=getScore()
                    print(score)
                    if(score < 40):
                        self.wfile.write(nameList[i])
                        print("detected face is " + nameList[i])
                        return
            self.wfile.write("no match")
            print("no match")
        return
    def do_POST(self):
        self.send_response(200)
        print('post')
   
httpd = HTTPServer(("", 9999), handle)
httpd.serve_forever()
