package com.leo.leoaigenplatform.ai;

import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:06
 **/
@SpringBootTest
class AiGenCodeServiceTest {

    @Resource
    private AiGenCodeService aiGenCodeService;

    @Test
    void generateHTML() {
        String userMessage = "请为我生成登陆页面，要求不超过20行代码";
        HTMLJsonStructure result = aiGenCodeService.generateHTML(userMessage);
        Assertions.assertNotNull(result);

    }

    @Test
    void generateMulti() {
        String userMessage = "请为我生成计算器，要求不超过50行代码";
        MultiJsonStructure result = aiGenCodeService.generateMulti(userMessage);
        Assertions.assertNotNull(result);
    }

    @Test
    void testChatMemory() {
        HTMLJsonStructure result = aiGenCodeService.generateHTMLWithChatMemory(1, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiGenCodeService.generateHTMLWithChatMemory(1, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
        result = aiGenCodeService.generateHTMLWithChatMemory(2, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiGenCodeService.generateHTMLWithChatMemory(2, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(result);
    }

}