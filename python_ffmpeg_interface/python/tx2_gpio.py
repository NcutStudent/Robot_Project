# arduino 10 get a call 
# arduino 11 hand up
# arduino 12 call phone
# arduino 13 is phone call(output)
class Pin:
    def __init__(self, path):
        self.path=path
        self.mode = 'in'

    def set_mode(self, mode):
        if not (mode == 'in' or mode == 'out'):
            return
        with open(self.path + 'direction', 'w') as direction:
            direction.write(mode)

    def get_value(self):
        with open(self.path + 'value'

class GPIO:
    def __init__(self):
        
