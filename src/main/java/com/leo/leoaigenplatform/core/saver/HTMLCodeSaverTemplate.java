package com.leo.leoaigenplatform.core.saver;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;

import java.io.File;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 18:16
 **/
public class HTMLCodeSaverTemplate extends AbstractCodeFileSaver<HTMLJsonStructure> {

    @Override
    protected CodeGenType getCodeType() {
        return CodeGenType.HTML;
    }

    @Override
    protected void saveFiles(HTMLJsonStructure result, String baseDirPath) {

        writeFile(baseDirPath, "index.html", result.getHtmlCode());

    }

    @Override
    protected void validateParam(HTMLJsonStructure result) {
        super.validateParam(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "html code is blank");
        }
    }
}
