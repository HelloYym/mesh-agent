FROM runner-container
COPY ./consumer-agent-netty/target/consumer-agent-netty-1.0.jar /root/dists/consumer-agent.jar
COPY ./provider-agent-netty/target/provider-agent-netty-1.0.jar /root/dists/provider-agent.jar
COPY start-agent.sh /usr/local/bin

RUN set -ex \
 && chmod a+x /usr/local/bin/start-agent.sh \
 && mkdir -p /root/logs

EXPOSE 8087
ENTRYPOINT ["docker-entrypoint.sh"]