package com.leo.leoaigenplatform.ai;

import com.leo.leoaigenplatform.model.enums.CodeGenType;
import dev.langchain4j.service.SystemMessage;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-06 13:06
 **/
public interface AiCodeGenTypeRoutingService {

    @SystemMessage(fromResource = "prompt/app-gen-type-prompt.txt")
    CodeGenType generateGenTypeBasedOnInitPrompt(String userMessage);

}