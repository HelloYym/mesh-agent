#!/usr/local/bin/python3

import argparse
import logging
import logging.config
import yaml
import sys

from benchmark.configuration import Configuration
from benchmark.task_agent import TaskAgent
from benchmark.workflow import Workflow


def bootstrap():
    config = init_config()
    task_agent = TaskAgent(config)

    task = task_agent.fetch_task()
    logging.info('task = %s', task)
    if task is None:
        logging.info('No task to execute, exit.')
        sys.exit(0)

    workflow = Workflow(config, task)
    result = workflow.run()

    task_agent.update_task(task, result)


def init_config():
    parser = argparse.ArgumentParser(
        description='Fetch the benchmark task then deploy and run.')
    parser.add_argument(
        '-c',
        '--config-file',
        help='configuration file',
        default='bootstrap.conf',
        type=argparse.FileType('r'))
    parser.add_argument(
        '-p',
        '--prefix',
        required=True,
        help='remote host prefix')
    argv = parser.parse_args()

    return Configuration(argv.prefix, argv.config_file)


if __name__ == '__main__':
    logging.config.dictConfig(yaml.load(open('logging.yml')))
    bootstrap()
