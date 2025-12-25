package com.leo.leoaigenplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:02
 **/
@Configuration
public class AiServiceAutoConfiguration {

    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Bean
    public AiGenCodeService aiService() {
//        return AiServices.create(AiGenCodeService.class, chatModel);
        return AiServices.builder(AiGenCodeService.class)
                .chatModel(chatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .build();

    }

}
