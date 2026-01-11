package com.leo.leoaigenplatform.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-08 18:55
 **/
@Slf4j
@Component
public class ToolManager {

    private final Map<String, BaseTool> toolMap = new HashMap<>();

    @Resource
    public BaseTool[] tools;

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具： {} -> {}", tool.getToolName(), tool.getToolCnName());
        }
        log.info("注册工具完成，共注册：{} 个工具", toolMap.size());
    }

    /**
     * 根据 工具名称 获取工具实例
     * @param toolName
     * @return
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取全部已注册的工具集合
     * @return
     */
    public BaseTool[] getAllTools() {
        return tools;
    }

}