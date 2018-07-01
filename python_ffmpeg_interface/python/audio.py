import pyaudio
import thread
import time

from utils.constants import *

class Audio_Capture:
    def __init__(self, tcp_client, srcKey, dstKey):
        self.tcp_client = tcp_client
        self.srcKey = srcKey
        self.dstKey = dstKey
        self.s      = tcp_client.get_udp_socket(srcKey)
        self.Host, self.Port = tcp_client.get_client_recv_ip_and_port(dstKey)
        while self.Host == 'null' or self.Port == 0:
            self.Host, self.Port = tcp_client.get_client_recv_ip_and_port(dstKey)
            time.sleep(1)
        
        self.p = pyaudio.PyAudio()
        self.recieveBuffer= []
        self.sendBuffer   = []
        self.flag         = False
        self.empty_sound  = "\x01" * frames_per_buffer * 2
        self.running = False
    
    def callback(self, in_data, frame_count, time_info, status):
        print(len(self.recieveBuffer), len(self.sendBuffer))

        if len(self.sendBuffer) < 50:
            self.sendBuffer.append(in_data)

        if self.flag and len(self.recieveBuffer) > 2 :
            del self.recieveBuffer[0]
            in_data = self.recieveBuffer[1]
        else:
            in_data = self.empty_sound
        return (in_data, pyaudio.paContinue)

    def sent_data(self):
        while self.stream.is_active():
            if len(self.sendBuffer) != 0:
                self.tcp_client.sent_data_to_server(self.s, self.sendBuffer[0], self.dstKey, (self.Host, self.Port))
                del self.sendBuffer[0]

    def recv_data(self):
         
        while self.stream.is_active():
            try:
                if len(self.recieveBuffer) < 60:
                    recieveData = self.s.recv(65505)
                    self.recieveBuffer.append(recieveData)
                else:
                    self.s.recvfrom(10240)
            except error:
                pass
 
            if len(self.recieveBuffer) > 3:
                self.flag = True
            if len(self.recieveBuffer) < 1:
                self.flag = False

    def start(self):
        if self.running == True:
            return
        self.running = True
        self.stream = self.p.open(format=self.p.get_format_from_width(WIDTH),
                channels=CHANNELS,
                rate=RATE,
                input=True,
                output=True,
                frames_per_buffer=frames_per_buffer,
                stream_callback=self.callback)

        self.stream.start_stream()
        thread.start_new_thread(self.sent_data, ())
        thread.start_new_thread(self.recv_data, ())

    def stop(self):
        self.stream.stop_stream()
        self.stream.close()
        self.running = False
