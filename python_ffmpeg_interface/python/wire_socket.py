from utils.constants import *
from camera import *
from audio import *
from gui import *


if tcp_client == None:
    exit()

if not tcp_client.input_pass(password):
    print("password error")
    exit()
'''
audio = Audio_Capture(tcp_client, RaspberryPiKey, CellPhoneKey)
audio.start()

camera = Video_Capture(tcp_client)
camera.transfer_start()
'''
root = tk.Tk()
gui = Call_GUI(root, tcp_client, RaspberryPiKey, CellPhoneKey, RLS, PLS)
gui.mainloop()
