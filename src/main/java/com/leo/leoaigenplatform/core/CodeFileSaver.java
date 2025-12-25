package com.leo.leoaigenplatform.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import com.leo.leoaigenplatform.constant.AppConstant;
import com.leo.leoaigenplatform.model.enums.CodeGenType;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:52
 **/
@Deprecated
public class CodeFileSaver {

    /**
     * 文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = AppConstant.FILE_SAVE_ROOT_DIR;

    /**
     * 保存 HTML 单个网页代码
     *
     * @param htmlJsonStructure
     * @return
     */
    public static File saveHtmlFile(HTMLJsonStructure htmlJsonStructure) {
        String path = buildUniqueFilePath(CodeGenType.HTML.getCode());
        saveFile(path, "index.html", htmlJsonStructure.getHtmlCode());
        return new File(path);
    }

    /**
     * 保存多文件代码
     *
     * @param multiJsonStructure
     * @return
     */
    public static File saveMultiFile(MultiJsonStructure multiJsonStructure) {
        String path = buildUniqueFilePath(CodeGenType.MULTI_FILE.getCode());
        saveFile(path, "index.html", multiJsonStructure.getHtmlCode());
        saveFile(path, "style.css", multiJsonStructure.getCssCode());
        saveFile(path, "script.js", multiJsonStructure.getJsCode());
        return new File(path);
    }

    /**
     * 构建文件唯一路径： tmp/code_output/bizType_雪花 ID
     *
     * @param bizType
     * @return
     */
    private static String buildUniqueFilePath(String bizType) {
        String uniquePathName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
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
    private static void saveFile(String path, String fileName, String content) {
        String finalPath = path + File.separator + fileName;
        FileUtil.writeString(content, finalPath, StandardCharsets.UTF_8);
    }
}
