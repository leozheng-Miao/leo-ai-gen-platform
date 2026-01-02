package com.leo.leoaigenplatform.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.leo.leoaigenplatform.ai.tools.FileWriteTool;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import com.leo.leoaigenplatform.service.ChatHistoryService;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    /**
     * Caffeine 内存从 Redis 中
     */
    private final Cache<String, AiGenCodeService> cache =
            Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(Duration.ofMinutes(30))
                    .expireAfterAccess(Duration.ofMinutes(10))
                    .removalListener((key, value, cause) -> {
                        log.debug("AI 服务实例被移除： appId = {}, cause = {}", key, cause);
                    })
                    .build();


    public AiGenCodeService getAiGenCodeService(long appId, CodeGenType type) {
        String key = buildCacheKey(appId, type);
        return cache.get(key, k -> createAiCodeGeneratorService(appId, type));
    }

    /**
     * 根据 传入 生成种类 调用不同服务生成 代码
     *
     * @param appId
     * @param type
     * @return
     */
    private AiGenCodeService createAiCodeGeneratorService(long appId, CodeGenType type) {
        log.info("为 appId = {} 创建新的 AI 服务实例", appId);
        MessageWindowChatMemory messageWindowChatMemory =
                MessageWindowChatMemory
                        .builder()
                        .id(appId)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();

        chatHistoryService.loadChatHistoryToMemory(appId, messageWindowChatMemory, 20);
        return switch (type) {
            case VUE_PROJECT -> AiServices.builder(AiGenCodeService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .tools(new FileWriteTool())
                    .chatMemoryProvider(memoryId -> messageWindowChatMemory) // 指定为 每个 memoryId绑定会话记忆
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called" + toolExecutionRequest.name()
                    ))
                    .build();
            case HTML, MULTI_FILE -> AiServices.builder(AiGenCodeService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(messageWindowChatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的类型" + type.getCode());
        };
    }

    /**
     * 兼容历史逻辑
     *
     * @param appId
     * @return
     */
    public AiGenCodeService getAiGenCodeService(long appId) {
        return getAiGenCodeService(appId, CodeGenType.HTML);
    }

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiGenCodeService aiCodeGeneratorService() {
        return getAiGenCodeService(0L);
    }


    /**
     * 根据 appId 和 生成代码种类构造 缓存 Key
     *
     * @param appId
     * @param type
     * @return
     */
    private String buildCacheKey(long appId, CodeGenType type) {
        return appId + "_" + type.getCode();
    }


}