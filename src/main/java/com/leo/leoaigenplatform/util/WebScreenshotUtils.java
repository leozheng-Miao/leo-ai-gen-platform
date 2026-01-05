package com.leo.leoaigenplatform.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-05 04:06
 **/
@Slf4j
public class WebScreenshotUtils {

    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();
    private final static int DEFAULT_WIDTH = 1600;
    private final static int DEFAULT_HEIGHT = 900;

    /**
     * 使用 线程池 获取 webDriver
     * @return
     */
    public static WebDriver getWebDriver(){
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            DRIVER_THREAD_LOCAL.set(driver);
        }
        return driver;
    }

    /**
     * 定时清理本地截图文件
     */
    public static void cleanupTempFiles() {

        String tempDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots" +
                File.separator;
        File localFile = new File(tempDir);
        if (localFile.exists()) {
            FileUtil.del(tempDir);
            log.info("本地截图文件已定时清理：{}", tempDir);
        }
    }

    @PreDestroy
    public void destroy() {
        getWebDriver().quit();
    }

    /**
     * 保存截图到本地
     * @param webUrl
     * @return 压缩后的截图路径
     */
    public static String saveWebPageScreenshot(String webUrl) {
        //1. 验证参数
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页 URL 不能为空");
            return null;
        }

        try {
            WebDriver webDriver = getWebDriver();
            //2. 创建临时目录
            String tempDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots" +
                    File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(tempDir);
            // 定义后缀 + 创建原始截图存储路径
            final String IMAGE_SUFFIX = ".png";
            String originScreenshotPath = tempDir + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
            //3. 访问页面
            webDriver.get(webUrl);
            //4. 等待页面加载
            waitLoadWebPage(webDriver);
            //5. 截图
            byte[] imageBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            //6. 保存原始图片
            saveImage(imageBytes, originScreenshotPath);
            //7. 压缩图片
            final String COMPRESS_SUFFIX = "_compress.jpg";
            String compressImagePath = tempDir + File.separator + RandomUtil.randomNumbers(5) + COMPRESS_SUFFIX;
            compressImage(originScreenshotPath, compressImagePath);
            log.info("压缩封面图片保存成功：{}", compressImagePath);
            //8. 删除初始图片
            FileUtil.del(originScreenshotPath);
            return compressImagePath;
        } catch (Exception e) {
            log.error("网页截图失败：{}", webUrl, e);
            return null;
        }
    }


    private static void waitLoadWebPage(WebDriver webDriver) {

        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            wait.until(driver ->
                    ((JavascriptExecutor) driver)
                            .executeScript("return document.readyState")
                            .equals("complete"));
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (InterruptedException e) {
            log.error("等待页面加载出现异常，继续执行截图", e);
        }

    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    /**
     * 保存封面图片
     *
     * @param imageBytes
     * @param imagePath
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (IORuntimeException e) {
            log.error("保存封面图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存封面图片失败");
        }

    }

    /**
     * 压缩封面图片
     *
     * @param originImagePath
     * @param targetPath
     */
    private static void compressImage(String originImagePath, String targetPath) {

        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    new File(originImagePath),
                    new File(targetPath),
                    COMPRESSION_QUALITY
            );
        } catch (IORuntimeException e) {
            log.error("压缩封面图片失败: {} -> {}", originImagePath, targetPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩封面图片失败");
        }
    }

}