import threading
from socket import *
class Tcp_Client:
    def __init__(self, host, port, udp_port, timeout=1):
        try:
            self.lock = threading.Lock()
            self.token = None
            self.server_udp_port = udp_port
            self.host = host
            self.udp_socket   = socket(AF_INET, SOCK_DGRAM)
            self.udp_socket.settimeout(1)
            self.tcp_socket = create_connection((host, port), timeout)
        except error:
            print('error ip or port')
            return
        
    def input_pass(self, password):
        try:
            self.lock.acquire()
            self.tcp_socket.send(password)
            data = self.tcp_socket.recv(1024)
            self.lock.release()
            if len(data) < 16:
                return False
            self.token = data
            self.tcp_socket.close()
            print(data)
            return True
        except error:
            print(error)
            return False;

    def get_tcp_socket(self, srcKey, dstKey):
        try:
            self.lock.acquire()
            self.udp_socket.sendto(self.token + ' t ' + dstKey, (self.host, self.server_udp_port))
            raw_data = self.udp_socket.recv(256)
            host, port = raw_data.split(' ')
            port = int(port)
            self.lock.release()
            sock = socket(AF_INET, SOCK_STREAM)
            sock.settimeout(1)
            sock.connect((host, port))
            sock.send(self.token + ' ' + srcKey)
            sock.recv(16)
        except error:
            return None
        return sock

    def get_udp_socket(self, key):
        sock = socket(AF_INET, SOCK_DGRAM)
        sock.settimeout(1)
        try:
            self.lock.acquire()
            sock.sendto(self.token + ' s ' + key , (self.host, self.server_udp_port))
            sock.recv(256)
            self.lock.release()
        except error:
            return None
        return sock

    def sent_data_to_server(self, socket, data, key, ip_port):
        socket.sendto(self.token + ' ' + key + ' ' + data, ip_port)
        return(self.token + ' ' + key + ' ' + data)

    def sent_tcp_data_to_server(self, socket, data, key):
        socket.sendall(self.token + ' ' + key + ' ' + data + '\r\n')

    def get_client_recv_ip_and_port(self, key):
        try:
            self.lock.acquire()
            self.udp_socket.sendto(self.token + ' g ' + key, (self.host, self.server_udp_port))
            raw_data = self.udp_socket.recv(256)
            self.lock.release()
            raw_data = raw_data.split(" ")
            return raw_data[0], int(raw_data[1])
        except error:
            return ('null', 0)

    def get_sliding_ip_and_port(self, key):
        try:
            self.lock.acquire()
            self.udp_socket.sendto(self.token + ' w ' + key, (self.host, self.server_udp_port))
            raw_data = self.udp_socket.recv(256)
            self.lock.release()
            raw_data = raw_data.split(" ")
            return raw_data[0], int(raw_data[1])
        except error:
            return ('null', 0)
