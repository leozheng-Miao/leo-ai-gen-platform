package com.leo.leoaigenplatform.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-28 21:51
 **/
@Configuration
@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisBeanStoreAutoConfiguration {

    private String host;
    private int port;
    private String password;
    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .ttl(ttl)
                .build();
    }


}