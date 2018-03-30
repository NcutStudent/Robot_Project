from tcp_client import *

server_ip = '140.128.88.166'
server_tcp_port = 7777
server_udp_port = 8888

WIDTH = 2
CHANNELS = 1
RATE = 44100
DURATION = 10

RaspberryPiKey = 'Raspberry'
CellPhoneKey   = 'Phone'

PLS = 'phoneListenSocket'
RLS = 'raspberryListenSocket'

password = "Robot"

Host = ''
Port = 0
recieve_port = 9487

frames_per_buffer = 10240

tcp_client = Tcp_Client(server_ip, server_tcp_port, server_udp_port)

