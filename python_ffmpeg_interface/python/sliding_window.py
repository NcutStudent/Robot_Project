import thread
import socket
import time
import threading

class Sliding_Window:
    IDLE = 0
    WAIT_FOR_RES = 1
    END = 2
    SendBufferMaxSize = 10
    def __init__(self, tcp_socket, windowSize, srcKey, dstKey):
        self.tcp_socket = tcp_socket
        self.socket = self.tcp_socket.get_udp_socket(srcKey)
        while self.socket == None:
            self.socket = self.tcp_socket.get_udp_socket(srcKey)

        ip_port = self.tcp_socket.get_sliding_ip_and_port(dstKey)
        while ip_port[0] == 'null':
            time.sleep(1)
            ip_port = self.tcp_socket.get_sliding_ip_and_port(dstKey)

        self.ip_port = ip_port
        self.key = dstKey
        self.myKey = srcKey
        self.stamp = 0
        self.recv_stamp = 0
        self.timeout = 0.02
        self.windows = [self.IDLE] * windowSize
        self.recv_windows = [self.IDLE] * windowSize
        self.windowsTime = [0] * windowSize
        self.windowsData = [None] * windowSize
        self.idle_count = windowSize
        self.sendBuffer = []
        self.recvBuffer = []
        self.lock = threading.Lock()
        self.isStop = False
        thread.start_new_thread(self.thread_recv, ())
        thread.start_new_thread(self.thread_check_timeout, ())

    def confirm_border(self, stamp, d):
        if (stamp + len(self.windows) - 1) % 128 < stamp:
            bot = (stamp + len(self.windows) - 1) % 128
            top = stamp
            if bot < d and d < top:
                return -1
            if stamp >= d:
                index = d - stamp
            else :
                index = d
        else :
            top = stamp + len(self.windows) - 1
            bot = stamp - 1
            if d < bot or top < d:
                return -1

            index = d - stamp
        return index

    def send(self, data):
        self.lock.acquire()
        if self.idle_count == 0 :
            self.lock.release()
            return False
        for i in range(len(self.windows)): 
            if self.windows[i] == self.IDLE:
                break
        data += bytearray([self.stamp + i])
        d = self.tcp_socket.sent_data_to_server(self.socket, data, self.myKey, self.ip_port)
        self.windowsTime[i] = time.time()
        self.windowsData[i] = data
        self.windows[i] = self.WAIT_FOR_RES
        self.idle_count -= 1
        self.lock.release()
        return True

    def getData(self) :
        data = None
        if len(self.recvBuffer) != 0 :
            data = self.recvBuffer[0]
            del self.recvBuffer[0]
        return data

    def recv(self):
        data = self.socket.recv(65505)
        if len(data) == 0:
            return
        if len(data) == 1:
            self.recvATK(data)
            return
        self.tcp_socket.sent_data_to_server(self.socket, data[-1], self.myKey, self.ip_port)
        index = self.confirm_border(self.recv_stamp, int(data[-1].encode('hex'), 16))
        if index == -1 :
            return
        if self.recv_windows[index] != self.IDLE :
            return

        self.recv_windows[index] = self.END
        length = len(self.recv_windows)
        print('get server atk : ' + str(int(data[-1].encode('hex'), 16)))
        for i in range(length) :
            if self.recv_windows[i] != self.END :
                break
            del self.recv_windows[0]
            self.recv_windows.append(self.IDLE)

        self.recv_stamp += i
        self.recv_stamp %= 128
        print("get data :" + str(len(data)))
        self.recvBuffer.append(data[:len(data) - 1])
        
    def thread_recv(self):
        while not self.isStop:
            try:
                self.recv()
            except socket.error :
                print("camera network recieve timeout")
                pass
        
    def check_timeout(self):
        now = time.time()
        i = 0
        for timeStamp in self.windowsTime:
            if self.windows[i] != self.WAIT_FOR_RES:
                continue
            if now - timeStamp > self.timeout:
                self.windowsTime[i] = time.time()
                self.tcp_socket.sent_data_to_server(self.socket, self.windowsData[i], self.key, self.ip_port)
        time.sleep(10)

    def thread_check_timeout(self):
        while not self.isStop:
            self.check_timeout()

    def recvATK(self, data):
        self.lock.acquire()
        print('recv atk : ' + str(int(data.encode('hex'), 16)))
        index = self.confirm_border(self.stamp, int(data.encode('hex'), 16))
        if index == -1:
            print('stamp error')
            self.lock.release()
            return
        if self.windows[index] != self.WAIT_FOR_RES:
            self.lock.release()
            return
        self.windows[index] = self.END
        i = 0
        for window in self.windows:
            if window != self.END:
                break;
            i += 1
        for o in range(i):
            del self.windows[0]
            self.windows.append(self.IDLE)
        self.idle_count += i
        self.stamp += i
        self.stamp %= 128
        self.lock.release()
