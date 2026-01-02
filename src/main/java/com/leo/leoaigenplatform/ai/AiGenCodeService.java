package com.leo.leoaigenplatform.ai;

import com.leo.leoaigenplatform.ai.jsonModel.AppNameJson;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 12:59
 **/
public interface AiGenCodeService {

    @SystemMessage(fromResource = "prompt/gen-html-prompt.txt")
    HTMLJsonStructure generateHTML(String userMessage);

    @SystemMessage(fromResource = "prompt/gen-multi-prompt.txt")
    MultiJsonStructure generateMulti(String userMessage);

    @SystemMessage(fromResource = "prompt/gen-html-prompt.txt")
    Flux<String> generateHTMLStream(String userMessage);

    @SystemMessage(fromResource = "prompt/gen-multi-prompt.txt")
    Flux<String> generateMultiStream(String userMessage);

    @SystemMessage(fromResource = "prompt/gen-app-name.txt")
    AppNameJson generateAppName(String userMessage);

    @SystemMessage(fromResource = "prompt/gen-html-prompt.txt")
    HTMLJsonStructure generateHTMLWithChatMemory(@MemoryId int memoryId, @UserMessage String userMessage);


}