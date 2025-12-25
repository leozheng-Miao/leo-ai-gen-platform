package com.leo.leoaigenplatform.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.constant.AppConstant;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.enums.CodeGenType;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 18:07
 **/
public abstract class AbstractCodeFileSaver<T> {

    /**
     * 文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.FILE_SAVE_ROOT_DIR;

    public final File saveCode(T result, Long appId) {
        //1. 校验参数
        validateParam(result);
        //2. 构建唯一目录
        String baseDirPath = buildUniqueFilePath(appId);
        //3. 保存文件 - 由子类实现
        saveFiles(result, baseDirPath);
        //4. 返回目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 验证输入参数
     * 可由子类覆盖
     *
     * @param result
     */
    protected void validateParam(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数不能为空");
        }
    }

    /**
     * 构建文件唯一路径： tmp/code_output/bizType_雪花 ID
     *
     * @return
     */
    protected String buildUniqueFilePath(Long appId) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "appId不能为空");
        }
        String bizType = getCodeType().getCode();
        String uniquePathName = StrUtil.format("{}_{}", bizType, appId);
        String finalPath = FILE_SAVE_ROOT_DIR + File.separator + uniquePathName;
        FileUtil.mkdir(finalPath);
        return finalPath;
    }

    /**
     * 保存单个文件
     *
     * @param path
     * @param fileName
     * @param content
     */
    protected static void writeFile(String path, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {
            String finalPath = path + File.separator + fileName;
            FileUtil.writeString(content, finalPath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码类型 - 由子类实现
     *
     * @return
     */
    protected abstract CodeGenType getCodeType();

    /**
     * 保存文件的具体实现 - 由子类实现
     *
     * @param result
     * @param baseDirPath
     */
    protected abstract void saveFiles(T result, String baseDirPath);

}
