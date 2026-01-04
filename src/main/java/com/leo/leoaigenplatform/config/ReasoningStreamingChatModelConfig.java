package com.leo.leoaigenplatform.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-02 13:13
 **/
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String apiKey;
    private String baseUrl;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .temperature(0.0) // 降低温度， 减少随机性
                .topP(0.8) // 限制采样范围
                .logRequests(true)
                .logResponses(true)
                .build();

    }


}