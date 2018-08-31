import os
import socket


class Workspace():

    def __init__(self, config, task):
        prefix = config.remote_host_prefix

        self.local = LocalHost(config, prefix, task)
        self.remote = RemoteHost(config, prefix, task)


class LocalHost():

    def __init__(self, config, prefix, task, expanduser=True):
        self.hostname = socket.gethostname()

        self.home = '{}/{}'.format(config.workspace_home, prefix)
        self.home = os.path.expanduser(self.home)

        self.lock_file = '{}/{}'.format(self.home, '.lock')
        self.dockerpwd_file = '{}/{}'.format(self.home, '.dockerpwd')

    def __repr__(self):
        s = ', '.join(['{}={}'.format(k, v) for k, v in self.__dict__.items()])
        return '{}({})'.format(self.__class__.__name__, s)


class RemoteHost():

    def __init__(self, config, prefix, task):
        self.hostname = '{}.{}'.format(prefix, socket.gethostname())
        self.user = config.remote_host_user

        self.home = '{}/{}'.format(config.workspace_home, prefix)
        self.team_home = '{}/{}'.format(self.home, task.team_id)
        self.task_home = '{}/{}'.format(self.team_home, task.task_id)

        self.lock_file = '{}/{}'.format(self.task_home, '.lock')
        self.dockerpwd_file = '{}/{}'.format(self.task_home, '.dockerpwd')

    def __repr__(self):
        s = ', '.join(['{}={}'.format(k, v) for k, v in self.__dict__.items()])
        return '{}({})'.format(self.__class__.__name__, s)
