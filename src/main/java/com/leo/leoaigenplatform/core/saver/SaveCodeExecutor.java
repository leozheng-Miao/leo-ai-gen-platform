package com.leo.leoaigenplatform.core.saver;

import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;

import java.io.File;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 18:22
 **/
public class SaveCodeExecutor {

    private static final HTMLCodeSaverTemplate htmlCodeSaverTemplate = new HTMLCodeSaverTemplate();
    private static final MultiCodeSaverTemplate multiCodeSaverTemplate = new MultiCodeSaverTemplate();

    public static File executeCodeSaver(CodeGenType codeGenType, Object result, Long appId) {
        return switch (codeGenType) {
            case HTML -> htmlCodeSaverTemplate.saveCode((HTMLJsonStructure) result, appId);
            case MULTI_FILE -> multiCodeSaverTemplate.saveCode((MultiJsonStructure) result, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "unsupported code gen type");
        };

    }

}
