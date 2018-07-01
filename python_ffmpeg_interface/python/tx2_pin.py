
class Pin:
    def __init__(self, gpio, path, pin_index):
        self.path=path
        self.mode       = 'in'
        self.gpio       = gpio
        self.is_open    = True
        self.pin_index  = pin_index

    def set_mode(self, mode):
        if not self.is_open:
            return None
        if not (mode == 'in' or mode == 'out'):
            return
        with open(self.path + 'direction', 'w') as direction:
            direction.write(mode)
            self.mode = mode

    def get_value(self):
        if self.is_open:
            with open(self.path + 'value', 'r') as f:
                return int(f.read())
        else:
            return None

    def set_value(self, value):
        if not self.is_open or self.mode == 'in':
            return
        with open(self.path + 'value', 'w') as f:
            f.write(str(value))

    def close(self):
        self.gpio.unregist(self.pin_index)

    def __del__(self):
        self.close()

