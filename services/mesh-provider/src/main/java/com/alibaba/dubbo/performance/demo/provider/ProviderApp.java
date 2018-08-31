package com.alibaba.dubbo.performance.demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ProviderApp {
    // 启动时请添加JVM参数:
    // -Ddubbo.protocol.port=20889 -Ddubbo.application.qos.enable=false -Dlogs.dir=/path/to/your/logs/dir

    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class,args);

        // 不让应用在docker中退出
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> System.out.println("do something..."),1000,5, TimeUnit.SECONDS);
    }
}
