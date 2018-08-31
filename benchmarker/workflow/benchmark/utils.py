import random
import socket
import string


def rand_string(digits, chars=string.ascii_letters + string.digits):
    return ''.join(random.choice(chars) for _ in range(digits))


def is_port_bound(port):
    port = int(port)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        # Try to bind to all IP addresses, this port
        s.bind(("", port))
        # If we get here, we were able to bind successfully,
        # which means the port is free.
    except socket.error:
        # If we get an exception, it might be because the port is still bound
        # which would be bad, or maybe it is a privileged port (<1024) and we
        # are not running as root, or maybe the server is gone, but sockets are
        # still in TIME_WAIT (SO_REUSEADDR). To determine which scenario, try
        # to connect.
        try:
            s.connect(("127.0.0.1", port))
            # If we get here, we were able to connect to something, which means
            # that the port is still bound.
            return True
        except socket.error:
            # An exception means that we couldn't connect, so a server probably
            # isn't still running on the port.
            pass
    finally:
        s.close()

    return False
