package com.leo.leoaigenplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-06 13:08
 **/
@Configuration
@Slf4j
public class AiCodeGenTypeRoutingServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}