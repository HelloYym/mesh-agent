#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer


class Server(BaseHTTPRequestHandler):
    def _set_headers(self, content_type='text/html'):
        self.send_response(200)
        self.send_header('Content-Type', content_type)
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write(
            bytes('<html><body><h1>hi!</h1></body></html>', 'utf-8'))

    def do_POST(self):
        self._set_headers(content_type='application/json')
        self.wfile.write(bytes("""
            {
                "errCode": 0,
                "errName": null,
                "errMsg": "success",
                "wrapped": true,
                "data": {
                    "teamId": 1000,
                    "taskid": 2000,
                    "gitpath": "https://code.aliyun.com/middlewarerace2018/agent-demo.git",
                    "imagepath": "registry.cn-hangzhou.aliyuncs.com/yym/middleware-race",
                    "imagerepouser": "yangym@zju.edu.cn",
                    "imagerepopassword": "yang@!#!2018"
                }
            }
        """, 'utf-8'))


def run(server_class=HTTPServer, handler_class=Server, port=3000):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print('Starting httpd...')
    httpd.serve_forever()


if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
