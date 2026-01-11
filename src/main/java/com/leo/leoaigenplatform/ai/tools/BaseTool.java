package com.leo.leoaigenplatform.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * @program: leo-ai-gen-platform
 * @description: 工具调用基类
 * @author: Miao Zheng
 * @date: 2026-01-08 18:24
 **/
public abstract class BaseTool {

    /**
     * 获取 工具方法 名称
     * @return
     */
    public abstract String getToolName();

    /**
     * 获取 工具中文名称
     * @return
     */
    public abstract String getToolCnName();

    /**
     * 工具请求时的返回值 - 交给前端显示
     * @return
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getToolCnName());
    }

    /**
     * 获取 工具执行结果 存储到数据库
     * @param arguments 工具执行参数
     * @return
     */
    public abstract String getToolExecutionResult(JSONObject arguments);

}