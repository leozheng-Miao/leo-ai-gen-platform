package com.leo.leoaigenplatform.langgraph4j.node;

import com.leo.leoaigenplatform.ai.service.AiCodeGenTypeRoutingService;
import com.leo.leoaigenplatform.langgraph4j.state.WorkflowContext;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import com.leo.leoaigenplatform.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            
            CodeGenType codeGenType;

            try {
                AiCodeGenTypeRoutingService routingService =
                        SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                codeGenType = routingService.generateGenTypeBasedOnInitPrompt(context.getOriginalPrompt());
                log.info("AI 智能路由完成，选择类型： {} ({})",codeGenType.getCode(), codeGenType.getDescription());
            } catch (Exception e) {
                log.error("AI 智能路由失败，使用默认HTML类型： {}", e.getMessage());
                codeGenType = CodeGenType.HTML;
            }
            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(codeGenType);
            return WorkflowContext.saveContext(context);
        });
    }
}