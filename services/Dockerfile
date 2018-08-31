FROM registry.cn-hangzhou.aliyuncs.com/aliware2018/debian-jdk8-devel

COPY . /root/workspace/services

WORKDIR /root/workspace/services
RUN set -ex && mvn clean package

COPY docker-entrypoint.sh /usr/local/bin
