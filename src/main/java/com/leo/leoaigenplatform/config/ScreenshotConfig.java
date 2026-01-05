package com.leo.leoaigenplatform.config;

import com.leo.leoaigenplatform.util.WebScreenshotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-05 15:38
 **/
@Configuration
@EnableScheduling
@Slf4j
public class ScreenshotConfig {

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempScreenshots() {
        try {
            WebScreenshotUtils.cleanupTempFiles();
        } catch (Exception e) {
            log.error("定时清理截图文件失败", e);
        }
    }
}