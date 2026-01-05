package com.leo.leoaigenplatform.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-05 14:06
 **/
@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String s = WebScreenshotUtils.saveWebPageScreenshot("https://www.codefather.cn");
        Assertions.assertNotNull(s);

    }
}