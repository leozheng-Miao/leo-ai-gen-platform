package com.leo.leoaigenplatform.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.leo.leoaigenplatform.service.ChatHistoryService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:02
 **/
@Configuration
@Slf4j
public class AiServiceAutoFactory {

    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel openAiStreamingChatModel;
    @Resource
    private ChatMemoryStore redisChatMemoryStore;
    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * Caffeine 内存从 Redis 中
     */
    private final Cache<Long, AiGenCodeService> cache =
            Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .expireAfterAccess(Duration.ofMinutes(10))
                    .removalListener((key, value, cause) -> {
                        log.debug("AI 服务实例被移除： appId = {}, cause = {}", key, cause);
                    })
                    .build();

//    @Bean
    public AiGenCodeService aiGenCodeService(long appId) {
        return cache.get(appId, this::getAiGenCodeService);
    }


    private AiGenCodeService getAiGenCodeService(long appId) {
//        return AiServices.create(AiGenCodeService.class, chatModel);
        log.info("为 appId = {} 创建新的 AI 服务实例", appId);
        MessageWindowChatMemory messageWindowChatMemory =
                MessageWindowChatMemory
                        .builder()
                        .id(appId)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();

        chatHistoryService.loadChatHistoryToMemory(appId, messageWindowChatMemory, 20);
        return AiServices.builder(AiGenCodeService.class)
                .chatModel(chatModel)
                .streamingChatModel(openAiStreamingChatModel)
                .chatMemory(messageWindowChatMemory)
                .build();
//        return AiServices.builder(AiGenCodeService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(openAiStreamingChatModel)
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
//                        .chatMemoryStore(redisChatMemoryStore)
//                        .maxMessages(20)
//                        .id(memoryId)
//                        .build())
//                .build();

    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiGenCodeService aiCodeGeneratorService() {
        return getAiGenCodeService(0L);
    }


}