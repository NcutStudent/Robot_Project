import httplib
import urllib
import sys

if len(sys.argv) is 3:
    http = httplib.HTTPConnection('127.0.0.1', 9999)
    f={'name' : str(sys.argv[1]), 'path': str(sys.argv[2])}
    param=urllib.urlencode(f)
    http.request("GET", "/?"+param);
    text=http.getresponse()
    data=text.read()
    print(data)
