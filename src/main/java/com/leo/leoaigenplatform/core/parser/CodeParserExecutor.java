package com.leo.leoaigenplatform.core.parser;

import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;

/**
 * @program: leo-ai-gen-platform
 * @description: 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 * @author: Miao Zheng
 * @date: 2025-12-23 17:59
 **/
public class CodeParserExecutor {

    private static final CodeParser<HTMLJsonStructure> htmlCodeParser = new HTMLCodeParser();

    private static final CodeParser<MultiJsonStructure> multiJsonStructureCodeParser = new MultiCodeParser();

    /**
     * 代码解析执行器方法
     *
     * @param codeContent
     * @param codeGenType
     * @return
     */
    public static Object executeCodeParser(String codeContent, CodeGenType codeGenType) {

        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiJsonStructureCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };

    }
}
