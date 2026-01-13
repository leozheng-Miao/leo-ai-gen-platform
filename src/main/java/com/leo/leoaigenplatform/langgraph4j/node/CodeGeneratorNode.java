package com.leo.leoaigenplatform.langgraph4j.node;

import com.leo.leoaigenplatform.constant.AppConstant;
import com.leo.leoaigenplatform.core.AiGenCodeFacade;
import com.leo.leoaigenplatform.langgraph4j.state.WorkflowContext;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import com.leo.leoaigenplatform.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            
            String userMessage = context.getEnhancedPrompt();
            CodeGenType generationType = context.getGenerationType();
            AiGenCodeFacade aiGenCodeFacade = SpringContextUtil.getBean(AiGenCodeFacade.class);
            log.info("开始生成代码，类型： {} ({})", generationType.getCode(), generationType.getDescription());
            Long appId = 0L;
            Flux<String> codeStream = aiGenCodeFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            codeStream.blockLast(Duration.ofMinutes(30));
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.FILE_SAVE_ROOT_DIR, generationType.getCode(), appId);
            log.info("AI 代码生成完成，生成目录：{}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            log.info("代码生成完成，目录: {}", generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}