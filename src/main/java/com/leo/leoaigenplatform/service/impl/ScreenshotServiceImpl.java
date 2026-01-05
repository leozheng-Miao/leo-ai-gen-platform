package com.leo.leoaigenplatform.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.manager.CosManager;
import com.leo.leoaigenplatform.service.ScreenshotService;
import com.leo.leoaigenplatform.util.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-05 14:21
 **/
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    /**
     * 生成 + 上传 截图 到 cos
     * @param webUrl
     * @return
     */
    @Override
    public String generateAddUploadScreenshot(String webUrl) {
        //1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "应用网页为空");
        //2. 本地截图并保存
        String localCompressPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        if (StrUtil.isBlank(localCompressPath)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成本地截图失败");
        }
        try {
            //3. 上传图片到 cos 对象存储
            String accessUrl = uploadScreenshotToCos(localCompressPath);
            ThrowUtils.throwIf(StrUtil.isBlank(accessUrl), ErrorCode.OPERATION_ERROR, "截图上传至 COS 失败");
            log.info("截图上传至 COS 成功：{} -> {}", webUrl, accessUrl);
            return accessUrl;
        } finally {
            //4. 清理本地文件
            cleanupLocalFile(localCompressPath);
        }
    }

    /**
     * 上传对象至 COS 对象存储
     * @param localCompressPath
     * @return
     */
    private String uploadScreenshotToCos(String localCompressPath) {
        File localFile = new File(localCompressPath);
        if (!localFile.exists()) {
            log.error("本地截图文件不存在：{}", localFile);
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compress.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadObject(cosKey, localFile);
    }

    /**
     * 生成 对象存储的 key
     * 格式： /screenshots/yyyy/MM/dd/fileName.jpg
     * @param fileName
     * @return
     */
    private String generateScreenshotKey(String fileName) {
        String dataPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", dataPath, fileName);
    }

    /**
     * 清理 本地截图文件以及其父文件夹
     * @param filePath
     */
    private void cleanupLocalFile(String filePath) {
        File localFile = new File(filePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理：{}", filePath);
        }
    }
}