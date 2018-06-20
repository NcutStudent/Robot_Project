import Tkinter as tk
import tkMessageBox as messagebox
import thread
import time
import socket
import tx2_gpio as gpio

gpio = gpio.GPIO()

from camera import *
from audio import *

class Call_GUI(tk.Frame):

    ON_PHONE_CALL = '\x42'
    ALIVE_CALL    = '\x53'
    HANG_UP_CALL  = '\x21'

    HAVE_PIN      = 398
    CALL_PIN      = 298
    HAND_PIN      = 389
    ANSR_PIN      = 388
    

    def __init__(self, master, tcp_client, srcKey, dstKey, listenIcmpKey, sendIcmpKey):
        tk.Frame.__init__(self, master)
        self.pack()
        self.tcp_client = tcp_client
        self.listenIcmpKey = listenIcmpKey
        self.srcKey = srcKey
        self.dstKey = dstKey
        self.sendIcmpKey = sendIcmpKey
        self.call_pin = gpio.open_pin(Call_GUI.CALL_PIN)
        self.call_pin.set_mode('in')
        self.hand_pin = gpio.open_pin(Call_GUI.HAND_PIN)
        self.hand_pin.set_mode('in')
        self.ansr_pin = gpio.open_pin(Call_GUI.ANSR_PIN)
        self.ansr_pin.set_mode('in')
        self.have_pin = gpio.open_pin(Call_GUI.HAVE_PIN)
        self.have_pin.set_mode('out')
        self.have_pin.set_value(0)
        self.call_phone_button = tk.Button(self, bg='yellow', height=4, width=20, font=("Courier", 44))
        self.call_phone_button['text'] = 'Call Phone'
        self.call_phone_button.grid(row=0, column=0)
        self.call_phone_button['command'] = lambda : self.action_call_phone()

        self.hang_up_button = tk.Button(self, bg='red', height=4, width=20, font=("Courier", 44))
        self.hang_up_button['text'] = 'Hang Up Phone'
        self.hang_up_button.grid(row=1, column=0)
        self.hang_up_button['command'] = lambda : self.action_hang_up()

        self.answer_button = tk.Button(self, bg='green', height=4, width=20, font=("Courier", 44))
        self.answer_button['text'] = 'Answer Phone'
        self.answer_button.grid(row=2, column=0)
        self.answer_button['command'] = lambda : self.action_answer_call()

        self.icmp_socket = tcp_client.get_udp_socket(listenIcmpKey)
        self.host_port   = tcp_client.get_client_recv_ip_and_port(sendIcmpKey)
        while self.host_port[1] == 0:
            self.host_port   = tcp_client.get_client_recv_ip_and_port(sendIcmpKey)

        self.is_calling     = False
        self.have_call      = False
        self.answer_call    = False
        self.hang_up_call   = False
        
        self.audio  = None
        self.camera = None

        thread.start_new_thread(self.listening_icmp, ())

    def action_call_phone(self) :
        if self.have_call | self.answer_call | self.hang_up_call | self.is_calling :
            return
        self.is_calling = True

    def action_answer_call(self) :
        if self.have_call == False and self.is_calling == False:
            return
        self.answer_call = True
        if self.audio != None :
            self.audio.stop()

        if self.camera != None :
            self.camera.transfer_stop()

        self.audio = Audio_Capture(tcp_client, self.srcKey, self.dstKey)
        self.camera= Video_Capture(tcp_client)

        self.audio.start()
        self.camera.transfer_start()

    def action_hang_up(self) :
        if not (self.have_call or self.is_calling or self.answer_call):
            return

        self.have_pin.set_value(0)
        self.hang_up_call = True
        print("HANG UP")

    def listening_icmp(self):
        lose_connection_count = 0
        active_udp_port_count = 0
        while True:
            if self.hand_pin.get_value() == 1:
                print("hand pin")
                self.action_hang_up()
            elif self.call_pin.get_value() == 1:
                self.action_call_phone()
                print("call pin")
            elif self.ansr_pin.get_value() == 1:
                print("anser call")
                self.action_answer_call()
            try:
                if self.hang_up_call :
                    self.tcp_client.sent_data_to_server(self.icmp_socket, Call_GUI.HANG_UP_CALL, self.sendIcmpKey, self.host_port)
                    self.is_calling     = False
                    self.have_call      = False
                    self.answer_call    = False
                    self.hang_up_call   = False
                    lose_connection_count = 0
                    active_udp_port_count = 0
                    if self.audio != None :
                        self.audio.stop()
  
                    if self.camera != None :
                        self.camera.transfer_stop()
  
                    self.audio  = None
                    self.camera = None

                elif self.is_calling :
                    self.tcp_client.sent_data_to_server(self.icmp_socket, Call_GUI.ON_PHONE_CALL, self.sendIcmpKey, self.host_port)
                    data = self.icmp_socket.recv(20)
                    if len(data) > 1:
                        continue;
                    print("recv data: " + data[0])
                    if data[0] == Call_GUI.HANG_UP_CALL :
                        self.hang_up_call = True
                    elif data[0] == Call_GUI.ALIVE_CALL or data[0] == Call_GUI.ON_PHONE_CALL :
                        self.action_answer_call()
                        self.is_calling  = False
                        self.answer_call = True
                        # parner answer the phone call
                        print("call alive")
                elif self.answer_call: 
                    if lose_connection_count > 2 :
                        self.hang_up_call = True
                        self.answer_call  = True
                        lose_connection_count = 0
                        # connection error
                        print("connection error")

                    t = time.time()
                    self.tcp_client.sent_data_to_server(self.icmp_socket, Call_GUI.ALIVE_CALL, self.sendIcmpKey, self.host_port)
                    print("send alive")
                    lose_connection_count += 1
                    data = self.icmp_socket.recv(20)
                    if len(data) > 1:
                        continue
                    print("recv data: " + data[0])
                    if data[0] == Call_GUI.HANG_UP_CALL :
                        self.hang_up_call = True
                        continue
                    elif data[0] == Call_GUI.ALIVE_CALL :
                        pass
                    else : 
                        continue
                    t = time.time() - t
                    t = 1 - t
                    if t > 0:
                        time.sleep(t)
                    lose_connection_count = 0
            
                else :
                    data = self.icmp_socket.recv(20)
                    if len(data) > 1:
                        continue
                    print("recv data: " + data[0])
                    if data[0] == Call_GUI.ALIVE_CALL :
                        self.hang_up_call = True
                    elif not self.have_call and data[0] == Call_GUI.ON_PHONE_CALL :
                        #have call
                        self.have_call = True
                        self.have_pin.set_value(1)
                        messagebox.showinfo("info", "you have a call")

                    if not self.have_call :
                        active_udp_port_count += 1
                        if active_udp_port_count > 60: 
                            self.icmp_socket.sendto(' ', self.host_port)
                            active_udp_port_count = 0
                            print('send a empty packet')
            except :
                if not self.have_call and not self.is_calling and not self.answer_call:
                    active_udp_port_count += 1
                    if active_udp_port_count > 60: 
                        self.icmp_socket.sendto(' ', self.host_port)
                        active_udp_port_count = 0
                        print('send a empty packet')
                
                pass
