package com.leo.leoaigenplatform.langgraph4j.node;

import com.leo.leoaigenplatform.core.builder.VueProjectBuilder;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.langgraph4j.state.WorkflowContext;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import com.leo.leoaigenplatform.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");
            
            // TODO: 实际执行项目构建逻辑
            CodeGenType generationType = context.getGenerationType();
            String generatedCodeDir = context.getGeneratedCodeDir();
            String buildResultDir;
            if (generationType == CodeGenType.VUE_PROJECT) {
                try {
                    VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                    boolean built = vueProjectBuilder.buildProject(generatedCodeDir);
                    if (built) {
                        buildResultDir = generatedCodeDir + File.separator + "dist";
                        log.info("Vue 倾慕构建成功， dist 目录： {}", buildResultDir);
                    } else {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                    }
                } catch (BusinessException e) {
                    log.error("Vue 项目构建异常：{}", e.getMessage(), e);
                    buildResultDir = generatedCodeDir;
                }
            } else {
                buildResultDir = generatedCodeDir;
            }
            // 更新状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            log.info("项目构建完成，结果目录: {}", buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}