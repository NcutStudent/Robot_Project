# arduino 10 get a call 
# arduino 11 hand up
# arduino 12 call phone
# arduino 13 is phone call(output)
import os

from tx2_pin import Pin

gpio_export_path    = '/sys/class/gpio/export'
gpio_unexport_path  = '/sys/class/gpio/unexport'
system_gpio_path    = '/sys/class/gpio/gpio%d/'

class GPIO:
    def __init__(self):
        self.regist_list = set()
    
    def open_pin(self, value):
        pin_path = system_gpio_path % value
        if not pin_path in self.regist_list:
            if os.path.exists(pin_path):
                self.regist_list.add(pin_path)
                return Pin(self, pin_path, value)
            with open(gpio_export_path, 'w') as f:
                f.write(str(value))
            if os.path.exists(pin_path):
                self.regist_list.add(pin_path)
                return Pin(self, pin_path, value)
        return None
        
    def unregist(self, pin_index):
        if not system_gpio_path%pin_index in self.regist_list:
            return

        with open(gpio_unexport_path, 'w') as f:
            f.write(str(pin_index))
        
        self.regist_list.remove(system_gpio_path%pin_index)
