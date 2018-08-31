package com.alibaba.dubbo.performance.demo.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ConsumerApp {
    // 启动时请添加JVM参数:
    // -Dlogs.dir=/path/to/your/logs/dir

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApp.class,args);
    }
}
