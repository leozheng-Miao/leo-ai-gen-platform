package com.leo.leoaigenplatform.core.parser;

/**
 * @program: leo-ai-gen-platform
 * @description: 代码解析器策略接口
 * @author: Miao Zheng
 * @date: 2025-12-23 17:55
 **/
public interface CodeParser<T> {

    /**
     * 代码解析方法
     *
     * @param codeContent
     * @return
     */
    T parseCode(String codeContent);
}
