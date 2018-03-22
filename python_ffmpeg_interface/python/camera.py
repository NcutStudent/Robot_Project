import numpy as np
import thread
import cv2
import libpython_interface
import socket
import multiprocessing
import time
import random
import sliding_window

tx2Camera = 'nvcamerasrc ! video/x-raw(memory:NVMM), width=(int)640, height=(int)480, format=(string)I420, framerate=(fraction)30/1 ! nvvidconv  ! video/x-raw, format=(string)I420 ! videoconvert ! video/x-raw, format=(string)BGR ! appsink'

class Video_Capture:
    RaspberryPiKey = "VideoRaspberry"
    PhoneKey    = "VideoPhone"
    def __init__(self, tcp_client):
        self.tcp_client = tcp_client
#        self.socket = tcp_client.get_tcp_socket(self.RaspberryPiKey, self.RaspberryPiKey)
#        self.Host, self.Port = tcp_client.get_client_recv_ip_and_port(self.RaspberryPiKey)
#        while self.Host == 'null' or self.Port == 0:
#            self.Host, self.Porttcp_client.get_client_recv_ip_and_port()
#            time.sleep(1)
        self.socket = sliding_window.Sliding_Window(tcp_client, 10, self.RaspberryPiKey, self.PhoneKey)
        self.cap = cv2.VideoCapture(tx2Camera)
        ret, frame = self.cap.read()
        if ret == False:
            raise Exception('can not open camera')
        self.h264Process = libpython_interface.H264ImgProcess(len(frame), len(frame[0]))
        self.receiveBuffer = []
        self.sendBuffer = []

    def set_output_size(self, width, height):
        if width < 10 or height < 10:
            return
        self.cap.set(3, width)
        self.cap.set(4, height)
        self.h264Process = libpython_interface.H264ImgProcess(width, height)
    
    def frame_stream(self):
        res, frame=self.cap.read()
        if not res :
            print("NOT")
            return
        data = self.h264Process.encodeImage(frame)
#        if len(self.sendBuffer) < 60:
#            self.sendBuffer.append(frame)
#        data = self.h264Process.encodeImage(frame)
        if len(data) == 0:
            return
        self.h264Process.pushImageData(data)
        frame = self.h264Process.getImage()
        if len(frame) == 0:
            return
        cv2.imshow('frame', frame)
        cv2.waitKey(1)

    def decode_frame(self) :
        data = self.socket.getData()
        if data == None:
            return
        data = np.fromstring(data, dtype=np.uint8)
        self.h264Process.pushImageData(data)
        frame = self.h264Process.getImage()
        if len(frame) == 0:
            return
        cv2.imshow('frame', frame)
        cv2.waitKey(1)

    def send_frame(self) :
        res, frame = self.cap.read()
        if not res:
            print("NO")
            return
        data = self.h264Process.encodeImage(frame)
        if len(data) == 0:
            return

#        self.tcp_client.sent_tcp_data_to_server\
#                    (self.socket, data.tostring(), self.RaspberryPiKey)
        flag = self.socket.send(data.tostring())
        print("send data")
        while not flag :
            time.sleep(0.02)
            flag = self.socket.send(data.tostring())
        time.sleep(0.1)

    def receive_frame(self) :
        try:
            data = self.socket.getData()
            if data == None:
                return
            if len(self.receiveBuffer) > 60:
                return
            self.receiveBuffer.append(data)
        except socket.error:
            print("TIMEOUTB")

    def close_stream(self):
        self.is_active = False

    def transfer_stream(self):
        while self.is_active :
            self.frame_stream()

    def decode_stream(self):
        while self.is_active:
            self.decode_frame()

    def send_stream(self):
        while self.is_active:
            self.send_frame()

    def receive_stream(self):
        while self.is_active:
            self.receive_frame()

    def transfer_start(self):
        self.is_active = True
#        self.thread_transfer = thread.start_new_thread(self.transfer_stream, ())
        self.thread_decode   = thread.start_new_thread(self.decode_stream,   ())
#        self.thread_receive  = thread.start_new_thread(self.receive_stream,  ())
        self.thread_send     = thread.start_new_thread(self.send_stream,     ())
