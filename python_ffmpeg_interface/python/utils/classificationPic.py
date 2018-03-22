import httplib
import urllib
import sys

if len(sys.argv) is 2:
    http = httplib.HTTPConnection('127.0.0.1', 9999)
    f={'path': str(sys.argv[1])}
    param=urllib.urlencode(f)
    http.request("GET", "/?"+param);
    text=http.getresponse()
    data=text.read()
    print(data)
