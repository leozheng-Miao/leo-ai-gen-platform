package com.leo.leoaigenplatform.core.saver;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;


/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 18:20
 **/
public class MultiCodeSaverTemplate extends AbstractCodeFileSaver<MultiJsonStructure> {

    @Override
    protected CodeGenType getCodeType() {
        return CodeGenType.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiJsonStructure result, String baseDirPath) {
        writeFile(baseDirPath, "index.html", result.getHtmlCode());
        writeFile(baseDirPath, "style.css", result.getCssCode());
        writeFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void validateParam(MultiJsonStructure result) {
        super.validateParam(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "html code is blank");
        }
//        if (StrUtil.isBlank(result.getCssCode())) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"css code is blank");
//        }
//        if (StrUtil.isBlank(result.getJsCode())) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"js code is blank");
//        }
    }
}
