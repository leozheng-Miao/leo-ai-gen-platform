package com.leo.leoaigenplatform.core;

import com.leo.leoaigenplatform.model.enums.CodeGenType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 14:15
 **/
@SpringBootTest
class AiGenCodeFacadeTest {

    @Resource
    private AiGenCodeFacade aiGenCodeFacade;

    @Test
    void generateAndSaveCode() {
        String userMessage = "请为我生成计算器页面，要求不超过50行代码";
//        File file = aiGenCodeFacade.generateAndSaveCode(userMessage, CodeGenType.HTML);
        File file = aiGenCodeFacade.generateAndSaveCode(userMessage, CodeGenType.MULTI_FILE, 0L);
        Assertions.assertNotNull(file);

    }

    @Test
    void generateAndSaveCodeStream() {
        String userMessage = "请为我生成用户详情页面，要求不超过50行代码";
//        File file = aiGenCodeFacade.generateAndSaveCode(userMessage, CodeGenType.HTML);
        Flux<String> result = aiGenCodeFacade.generateAndSaveCodeStream(userMessage, CodeGenType.HTML, 0L);
        List<String> list = result.collectList().block();
        Assertions.assertNotNull(list);
        String completeContent = String.join("", list);
        Assertions.assertNotNull(completeContent);

    }

    @Test
    void generateVueProjectStream() {
        String userMessage = "简单的任务记录网站，要求不超过200行代码";
//        File file = aiGenCodeFacade.generateAndSaveCode(userMessage, CodeGenType.HTML);
        Flux<String> result = aiGenCodeFacade.generateAndSaveCodeStream(userMessage, CodeGenType.VUE_PROJECT, 20L);
        List<String> list = result.collectList().block();
        Assertions.assertNotNull(list);
        String completeContent = String.join("", list);
        Assertions.assertNotNull(completeContent);
    }
}