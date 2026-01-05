package com.leo.leoaigenplatform.service;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-05 14:19
 **/
public interface ScreenshotService {

    /**
     * 生成 + 上传 截图 到 cos
     * @param webUrl
     * @return
     */
    String generateAddUploadScreenshot(String webUrl);

}