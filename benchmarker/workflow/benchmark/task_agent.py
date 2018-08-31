import requests
import socket

from benchmark.model.task import Task
from benchmark.utils import rand_string
from logging import getLogger


class TaskAgent():

    def __init__(self, config):
        self.config = config
        self.logger = getLogger(__name__)
        self.daemon_id = self.__get_daemon_id()

    def fetch_task(self):
        self.logger.info('>>> Fetch task.')

        url = self.config.task_fetch_url
        payload = self.__create_fetch_task_payload()
        try:
            response = self.__send_request(url, payload)

            if response['errCode'] != 0:
                self.logger.error(
                    'Failed to fetch task. %s', response['errMsg'])
                return None

            data = response['data']
            if data is None:
                self.logger.info('No task fetched.')
                return None

            return Task(data)
        except requests.exceptions.RequestException as err:
            self.logger.exception('Failed to fetch task.')
            return None

    def update_task(self, task, data):
        self.logger.info('>>> Update task.')

        url = self.config.task_update_url
        payload = self.__create_update_task_payload(task, data)
        try:
            self.__send_request(url, payload)
        except requests.exceptions.RequestException as err:
            self.logger.exception('Failed to update task.')

    def __get_daemon_id(self):
        return '{}[{}]'.format(socket.gethostname(), rand_string(7))

    def __create_fetch_task_payload(self):
        payload = {}
        payload['token'] = self.config.access_token
        payload['raceId'] = self.config.race_id
        payload['daemonid'] = self.daemon_id
        return payload

    def __create_update_task_payload(self, task, data):
        payload = {}
        payload['token'] = self.config.access_token
        payload['taskid'] = task.task_id
        payload['daemonid'] = self.daemon_id

        payload['status'] = data.get('status')
        payload['isvalid'] = data.get('is_valid')
        payload['message'] = data.get('message')
        payload['rank'] = data.get('rank')
        payload['scoreJson'] = data.get('scoreJson')
        return payload

    def __send_request(self, url, payload):
        self.logger.debug('fetch task url = %s', url)
        masked_payload = {**payload}
        masked_payload['token'] = '******'
        self.logger.debug('request data = %s', masked_payload)

        r = requests.post(url, json=payload, timeout=60)
        r.raise_for_status()
        return r.json()
