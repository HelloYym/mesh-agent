class Task():

    def __init__(self, dict):
        self.team_id = dict['teamId']
        self.task_id = dict['taskid']
        self.code_path = dict['gitpath']
        self.image_path = dict['imagepath']
        self.docker_host = self.image_path.split('/')[0]
        self.docker_username = dict['imagerepouser']
        self.docker_password = dict['imagerepopassword']

    def __repr__(self):
        data = []
        for k, v in self.__dict__.items():
            if k == 'docker_password':
                v = v[:3] + '******' + v[-2:]
            data.append('{}={}'.format(k, v))
        return '{}({})'.format(self.__class__.__name__, ', '.join(data))
