package com.leo.leoaigenplatform.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.leo.leoaigenplatform.ai.AiGenCodeService;
import com.leo.leoaigenplatform.ai.AiServiceAutoFactory;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.ai.model.message.AiResponseMessage;
import com.leo.leoaigenplatform.ai.model.message.ToolExecutedMessage;
import com.leo.leoaigenplatform.ai.model.message.ToolRequestMessage;
import com.leo.leoaigenplatform.core.parser.CodeParserExecutor;
import com.leo.leoaigenplatform.core.saver.SaveCodeExecutor;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @program: leo-ai-gen-platform
 * @description: 门面设计模式 实现 文档解析 和 保存
 * @author: Miao Zheng
 * @date: 2025-12-23 13:52
 **/
@Service
@Slf4j
public class AiGenCodeFacade {

    @Resource
    private AiServiceAutoFactory aiServiceAutoFactory;
    @Resource
    private AiGenCodeService aiGenCodeService;

    /**
     * 生成代码并保存
     *
     * @param userMessage
     * @param codeType
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenType codeType, Long appId) {

        if (codeType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "codeType is null");
        }
        AiGenCodeService service = aiServiceAutoFactory.getAiGenCodeService(appId);
        return switch (codeType) {
            case HTML -> {
                HTMLJsonStructure htmlJsonStructure = service.generateHTML(userMessage);
                yield SaveCodeExecutor.executeCodeSaver(codeType, htmlJsonStructure, appId);
            }
            case MULTI_FILE -> {
                MultiJsonStructure multiJsonStructure = service.generateMulti(userMessage);
                yield SaveCodeExecutor.executeCodeSaver(codeType, multiJsonStructure, appId);
            }
            default -> {
                String errorMessage = "目前不支持此格式的类型" + codeType.getCode();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };

    }

    /**
     * 流式生成代码并保存
     *
     * @param userMessage
     * @param codeType
     * @param appId
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenType codeType, Long appId) {

        if (codeType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "codeType is null");
        }

        AiGenCodeService aiGenCodeService = aiServiceAutoFactory.getAiGenCodeService(appId, codeType);

        return switch (codeType) {
            case HTML -> {
                Flux<String> result = aiGenCodeService.generateHTMLStream(userMessage);
                yield processCodeStream(result, codeType, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiGenCodeService.generateMultiStream(userMessage);
                yield processCodeStream(result, codeType, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiGenCodeService.generateVueProjectStreaming(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "目前不支持此格式的类型" + codeType.getCode();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };

    }

    /**
     * 处理代码流
     *
     * @param codeStream
     * @param codeGenType
     * @param appId
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenType codeGenType, Long appId) {

        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeCode = codeBuilder.toString();
                        Object codeParser = CodeParserExecutor.executeCodeParser(completeCode, codeGenType);
                        File completeFile = SaveCodeExecutor.executeCodeSaver(codeGenType, codeParser, appId);
                        log.info("文件保存成功，文件路径为:{}", completeFile.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("文件保存失败: {}", e.getMessage());
                    }
                });
    }


    /**
     * 处理token Stream
     * @param tokenStream
     * @return
     */
//    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
//
//        return Flux.create(sink -> {
//            AtomicInteger toolCallCount = new AtomicInteger(0);
//            AtomicBoolean isCompleted = new AtomicBoolean(false);
//            // 跟踪已创建的文件路径
//            Set<String> createdFiles = Collections.synchronizedSet(new HashSet<>());
//            // 保存 StreamingHandle 的引用，用于取消流
//            AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();
//            tokenStream
////                    .onPartialResponse((String response) -> {
////                        if (isCompleted.get()) return;
////                        AiResponseMessage aiResponseMessage = new AiResponseMessage(response);
////                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
////                    })
//                    .onPartialResponseWithContext((partialResponse, context) -> {
//                        // 保存 handle 引用
//                        if (handleRef.get() == null) {
//                            handleRef.set(context.streamingHandle());
//                        }
//
//                        if (isCompleted.get()) {
//                            log.warn("[{}] 流已完成，取消底层连接", appId);
//                            context.streamingHandle().cancel();
//                            return;
//                        }
//                        String response = partialResponse.text();
//                        AiResponseMessage aiResponseMessage = new AiResponseMessage(response);
//                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
//                    })
//                    .beforeToolExecution((toolExecutionRequest) -> {
//                        if (isCompleted.get()) {
//                            log.warn("[{}] 流已完成，忽略工具执行请求", appId);
//                            // 尝试取消流
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            return;
//                        }
//                        int count = toolCallCount.incrementAndGet();
//
//                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
//
//                        String arguments = toolRequestMessage.getArguments();
//                        log.info("[{}] 工具调用 #{}: name={}, args={}",
//                                appId, count, toolRequestMessage.getName(), arguments);
//
//                        try {
//                            JSONObject args = JSONUtil.parseObj(arguments);
//                            String filePath = args.getStr("relativePath");
//
//                            // 检测重复文件创建 - DeepSeek 的主要问题
//                            if (filePath != null) {
//                                if (createdFiles.contains(filePath)) {
//                                    log.error("[{}] ❌ 检测到重复创建文件: {}，DeepSeek 模型进入循环，强制停止",
//                                            appId, filePath);
//                                    isCompleted.set(true);
//                                    // 关键：取消底层流
//                                    if (handleRef.get() != null) {
//                                        handleRef.get().cancel();
//                                    }
//                                    sink.complete();
//                                    return;
//                                }
//                                createdFiles.add(filePath);
//                                log.info("[{}] ✅ 记录新文件: {}，当前已创建 {} 个文件",
//                                        appId, filePath, createdFiles.size());
//                            }
//                        } catch (Exception e) {
//                            log.warn("[{}] 解析工具参数失败: {}", appId, e.getMessage());
//                        }
//
//                        // DeepSeek 安全阀：超过 40 次工具调用强制停止
//                        if (count > 30) {
//                            log.error("[{}] ❌ 工具调用次数超过 30 次，DeepSeek 可能失控，强制停止", appId);
//                            isCompleted.set(true);
//                            // 关键：取消底层流
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            sink.complete();
//                            return;
//                        }
//
//                        // DeepSeek 智能停止：合理的文件数量后停止
//                        // Vue 项目通常需要 10-25 个文件
//                        if (createdFiles.size() >= 25) {
//                            log.warn("[{}] ⚠️ 已创建 {} 个文件，达到上限，强制停止",
//                                    appId, createdFiles.size());
//                            isCompleted.set(true);
//                            // 关键：取消底层流
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            sink.complete();
//                            return;
//                        }
//                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
//                    })
//                    .onToolExecuted((ToolExecution toolExecution) -> {
//                        if (isCompleted.get()) {
//                            log.warn("[{}] 流已完成，忽略工具执行结果", appId);
//                            // 关键：取消底层流
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            return;
//                        }
//                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
//                        log.info("[{}] 工具执行完成: name={}, result={}",
//                                appId,
//                                toolExecution.request().name(),
//                                toolExecution.result().substring(0, Math.min(50, toolExecution.result().length())));
//                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
//                    })
//                    .onIntermediateResponse((ChatResponse response) -> {
//                        log.debug("收到中间响应， 继续等待后续工具执行");
//                        if (isCompleted.get()) {
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            return;
//                        }
//                        boolean hasTools = response.aiMessage().hasToolExecutionRequests();
//                        log.info("[{}] 中间响应: finishReason={}, hasTools={}, toolCount={}, filesCount={}",
//                                appId,
//                                response.finishReason(),
//                                hasTools,
//                                toolCallCount.get(),
//                                createdFiles.size());
//
//                        // DeepSeek 特殊处理：如果说 STOP 且无工具，立即停止
//                        if (response.finishReason() == FinishReason.STOP && !hasTools) {
//                            log.info("[{}] ✅ DeepSeek 发出 STOP 信号且无工具请求，结束流", appId);
//                            isCompleted.set(true);
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            sink.complete();
//                            return;
//                        }
//
//                        // DeepSeek 智能判断：创建了足够的文件且没有新工具
//                        if (createdFiles.size() >= 8 && !hasTools) {
//                            log.info("[{}] ✅ 已创建 {} 个文件且无新工具请求，判断为完成",
//                                    appId, createdFiles.size());
//                            isCompleted.set(true);
//                            if (handleRef.get() != null) {
//                                handleRef.get().cancel();
//                            }
//                            sink.complete();
//                        }
//                    })
//                    .onCompleteResponse((ChatResponse response) -> {
//                        if (isCompleted.get()) {
//                            log.debug("[{}] 流已在中间响应中完成", appId);
//                            return;
//                        }
//                        log.info("[{}] ✅ 最终响应: finishReason={}, 总工具调用={}, 创建文件={}",
//                                appId, response.finishReason(), toolCallCount.get(), createdFiles.size());
//                        isCompleted.set(true);
//                        sink.complete();
//                    })
//                    .onError((Throwable error) -> {
//                        log.error("[{}] ❌ 流式生成错误", appId, error);
//                        isCompleted.set(true);
//                        // 发生错误时也要取消流
//                        if (handleRef.get() != null) {
//                            try {
//                                handleRef.get().cancel();
//                            } catch (Exception e) {
//                                log.warn("[{}] 取消流时发生异常", appId, e);
//                            }
//                        }
//                        sink.error(error);
//                    })
//                    .start();
//        });
//    }

    /**
     * 处理token Stream
     *
     * @param tokenStream
     * @return
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {

        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse((String response) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(response);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .beforeToolExecution((toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        log.info("[{}] 工具执行完成: name={}, result={}",
                                appId,
                                toolExecution.request().name(),
                                toolExecution.result().substring(0, Math.min(50, toolExecution.result().length())));
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

    /**
     * 生成应用名称
     *
     * @param userMessage
     * @return
     */
    public String generateAppName(String userMessage) {
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "userMessage is null");
        return aiGenCodeService.generateAppName(userMessage).getAppName();
    }

}