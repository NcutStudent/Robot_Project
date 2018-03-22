import pyaudio
import time
import sys
import threading
import thread
import os
import cv2
import multiprocessing

from socket import *
from tcp_client import *
from camera import *

<<<<<<< HEAD:Raspberry_Pi/wire_socket.py
server_ip = '140.128.88.166'
=======
if os.getuid() != 0:
    print("this program require admin")
    exit()

server_ip = '192.168.0.138'
>>>>>>> 79f8f1c5e2539acd12da2444398f6e96b4753966:python_ffmpeg_interface/python/wire_socket.py
server_tcp_port = 7777
server_udp_port = 8888

WIDTH = 2
CHANNELS = 1
RATE = 44100
DURATION = 10

RaspberryPiKey = 'Raspberry'
#RaspberryPiKey = 'Phone'
CellPhoneKey   = 'Phone'

Host = ''
Port = 0
recieve_port = 9487

frames_per_buffer = 10240

#pool = multiprocessing.Pool()

tcp_client = Tcp_Client(server_ip, server_tcp_port, server_udp_port)

if tcp_client == None:
    exit()

if not tcp_client.input_pass("Robot"):
    print("password error")
    exit()
s = tcp_client.get_udp_socket(RaspberryPiKey)

def thread_updater():
    global Host
    global Port
    host, port = tcp_client.get_client_recv_ip_and_port(CellPhoneKey) # get the phone ip and port
    while host == 'null' or port == 0:
        host, port = tcp_client.get_client_recv_ip_and_port(CellPhoneKey)
        time.sleep(1)

    Host = host
    Port = port

thread_updater()

print(Host)
print(Port)

p = pyaudio.PyAudio()

sendBuffer   = []
recieveBuffer= []
flag = False

empty_sound = "\x01" * frames_per_buffer * 2

def callback(in_data, frame_count, time_info, status):
    global sendBuffer
    print(len(recieveBuffer), len(sendBuffer))
    now_time = time.time()

    if len(sendBuffer) < 50:
        sendBuffer.append(in_data)
    
    if flag and len(recieveBuffer) > 1 :
        del recieveBuffer[0]
        in_data = recieveBuffer[1]
    else:
        in_data = empty_sound
    return (in_data, pyaudio.paContinue)

stream = p.open(format=p.get_format_from_width(WIDTH),
                channels=CHANNELS,
                rate=RATE,
                input=True,
                output=True,
#                input_device_index=2,
                frames_per_buffer=frames_per_buffer,
                stream_callback=callback)

stream.start_stream()

def sent_data(stream, sendBuffer):
    while stream.is_active():
        if len(sendBuffer) != 0:
            tcp_client.sent_data_to_server(s, sendBuffer[0], CellPhoneKey, (Host, Port))
            del sendBuffer[0]

thread.start_new_thread(sent_data, (stream, sendBuffer))
#threading.Thread(target=sent_data, args=(stream, sendBuffer)).start()

#TODO START
camera = Video_Capture(tcp_client)
camera.transfer_start()
#TODO END

stream_is_active = True
stream_time = time.time()




tx2Camera = 'nvcamerasrc ! video/x-raw(memory:NVMM), width=(int)640, height=(int)480, format=(string)I420, framerate=(fraction)30/1 ! nvvidconv  ! video/x-raw, format=(string)I420 ! videoconvert ! video/x-raw, format=(string)BGR ! appsink'
#cap = cv2.VideoCapture(tx2Camera)

while stream_is_active:
    try:
        if len(recieveBuffer) < 60:
            recieveData = s.recv(65505)
            recieveBuffer.append(recieveData)
        else:
            s.recvfrom(10240)
    except error:
#        print("timeoutA")
        pass

    if len(recieveBuffer) > 10:
        flag = True
    if len(recieveBuffer) < 2:
        flag = False
    if time.time() - stream_time > 0.5:
        print("check alive")
        stream_time = time.time()
        stream_is_alive = stream.is_active()


stream.stop_stream()
stream.close()
p.terminate()
camera.close_stream()
