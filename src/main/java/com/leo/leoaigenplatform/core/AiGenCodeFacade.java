package com.leo.leoaigenplatform.core;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.AiGenCodeService;
import com.leo.leoaigenplatform.ai.AiServiceAutoFactory;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.core.parser.CodeParserExecutor;
import com.leo.leoaigenplatform.core.saver.SaveCodeExecutor;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
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
        AiGenCodeService service = aiServiceAutoFactory.aiGenCodeService(appId);
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
     * @param userMessage
     * @param codeType
     * @param appId
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenType codeType, Long appId) {

        if (codeType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "codeType is null");
        }

        AiGenCodeService aiGenCodeService = aiServiceAutoFactory.aiGenCodeService(appId);

        return switch (codeType) {
            case HTML -> {
                Flux<String> result = aiGenCodeService.generateHTMLStream(userMessage);
                yield processCodeStream(result, codeType, appId);
            }
            case MULTI_FILE -> {
                Flux<String> result = aiGenCodeService.generateMultiStream(userMessage);
                yield processCodeStream(result, codeType, appId);
            }
            default -> {
                String errorMessage = "目前不支持此格式的类型" + codeType.getCode();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };

    }

    /**
     * 处理代码流
     * @param codeStream
     * @param codeGenType
     * @param appId
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenType codeGenType,Long appId) {

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
     * 生成应用名称
     * @param userMessage
     * @return
     */
    public String generateAppName(String userMessage) {
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "userMessage is null");
        return aiGenCodeService.generateAppName(userMessage).getAppName();
    }

}