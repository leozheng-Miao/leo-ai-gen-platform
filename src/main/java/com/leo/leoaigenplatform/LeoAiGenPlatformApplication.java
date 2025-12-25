package com.leo.leoaigenplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.leo.leoaigenplatform.mapper")
public class LeoAiGenPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeoAiGenPlatformApplication.class, args);
    }

}
