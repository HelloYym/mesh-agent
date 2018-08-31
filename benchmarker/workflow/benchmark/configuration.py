import configparser

DEFAULT_SECTION = 'Default'
TIANCHI_SECTION = 'Tianchi'
WORKSPACE_SECTION = 'Workspace'
SERVICES_SECTION = 'Services'
WRK_SECTION = 'Wrk'
DOCKER_SECTION = 'Docker'


class Configuration():

    def __init__(self, prefix, config_file):
        self.prefix = prefix
        self.config = configparser.ConfigParser(allow_no_value=True)
        self.config.read_file(config_file)

    def __get_value(self, key, section=DEFAULT_SECTION):
        return self.config[section][key]

    @property
    def consumer_app_sha256(self):
        return self.__get_value('ConsumerAppSha256')

    @property
    def provider_app_sha256(self):
        return self.__get_value('ProviderAppSha256')

    @property
    def entrypoint_script_sha256(self):
        return self.__get_value('EntrypointScriptSha256')

    @property
    def access_token(self):
        return self.__get_value('Token', TIANCHI_SECTION)

    @property
    def race_id(self):
        return self.__get_value('RaceId', TIANCHI_SECTION)

    @property
    def task_fetch_url(self):
        tianchi_host = self.__get_value('Host', TIANCHI_SECTION)
        task_fetch_path = self.__get_value('TaskFetchPath', TIANCHI_SECTION)
        return '{}{}'.format(tianchi_host, task_fetch_path)

    @property
    def task_update_url(self):
        tianchi_host = self.__get_value('Host', TIANCHI_SECTION)
        task_update_path = self.__get_value('TaskUpdatePath', 'Tianchi')
        return '{}{}'.format(tianchi_host, task_update_path)

    @property
    def workspace_home(self):
        return self.__get_value('Home', WORKSPACE_SECTION)

    @property
    def remote_host_user(self):
        return self.__get_value('RemoteHostUser', WORKSPACE_SECTION)

    @property
    def remote_host_prefix(self):
        return self.prefix

    @property
    def max_attempts(self):
        return self.__get_value('MaxAttempts', SERVICES_SECTION)

    @property
    def sleep_interval(self):
        return self.__get_value('SleepInterval', SERVICES_SECTION)

    @property
    def wrk_threads(self):
        return self.__get_value('Threads', WRK_SECTION)

    @property
    def wrk_timeout(self):
        return self.__get_value('Timeout', WRK_SECTION)

    @property
    def warmup_duration(self):
        return self.__get_value('WarmupDuration', WRK_SECTION)

    @property
    def pressure_duration(self):
        return self.__get_value('PressureDuration', WRK_SECTION)

    @property
    def small_scale(self):
        return self.__get_value('SmallScale', WRK_SECTION)

    @property
    def medium_scale(self):
        return self.__get_value('MediumScale', WRK_SECTION)

    @property
    def large_scale(self):
        return self.__get_value('LargeScale', WRK_SECTION)

    @property
    def cpu_period(self):
        return self.__get_value('CpuPeriod', DOCKER_SECTION)

    @property
    def etcd_cpu_quota(self):
        return self.__get_value('EtcdCpuQuota', DOCKER_SECTION)

    @property
    def etcd_memory(self):
        return self.__get_value('EtcdMemory', DOCKER_SECTION)

    @property
    def small_provider_cpu_quota(self):
        return self.__get_value('SmallProviderCpuQuota', DOCKER_SECTION)

    @property
    def small_provider_memory(self):
        return self.__get_value('SmallProviderMemory', DOCKER_SECTION)

    @property
    def medium_provider_cpu_quota(self):
        return self.__get_value('MediumProviderCpuQuota', DOCKER_SECTION)

    @property
    def medium_provider_memory(self):
        return self.__get_value('MediumProviderMemory', DOCKER_SECTION)

    @property
    def large_provider_cpu_quota(self):
        return self.__get_value('LargeProviderCpuQuota', DOCKER_SECTION)

    @property
    def large_provider_memory(self):
        return self.__get_value('LargeProviderMemory', DOCKER_SECTION)

    @property
    def consumer_cpu_quota(self):
        return self.__get_value('ConsumerCpuQuota', DOCKER_SECTION)

    @property
    def consumer_memory(self):
        return self.__get_value('ConsumerMemory', DOCKER_SECTION)
