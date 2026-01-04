package com.leo.leoaigenplatform.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-04 17:35
 **/
@Slf4j
@Component
public class VueProjectBuilder {


    public void buildProjectAsync(String sourceDir) {
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(sourceDir);
                    } catch (Exception e) {
                        log.error("异步构建 Vue 项目时发生异常：{}", e.getMessage(), e);
                    }
                });
    }
    /**
     * 构建项目
     * @param sourceDir
     * @return
     */
    public boolean buildProject(String sourceDir) {

        File projectDir = new File(sourceDir);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectDir);
            return false;
        }
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在：{}", packageJson.getAbsolutePath());
            return false;
        }

        log.info("开始构建 Vue 项目：{}", projectDir);

        if(!installCommend(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        if(!runBuildCommend(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }

        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成：{}", distDir.getAbsolutePath());
            return false;
        }
        log.info("项目构建成功，dist目录：{}", distDir.getAbsolutePath());
        return true;

    }

    /**
     * 执行 npm install  命令
     * @param dir
     * @return
     */
    private boolean installCommend(File dir) {

        log.info("执行 npm install ...");
        String commend = String.format("%s install", buildCommend("npm"));
        return executeCommend(dir, commend, 300);
    }

    /**
     * 执行 npm run build  命令
     * @param dir
     * @return
     */
    private boolean runBuildCommend(File dir) {

        log.info("执行 npm run build ...");
        String commend = String.format("%s run build", buildCommend("npm"));
        return executeCommend(dir, commend, 300);
    }

    private String buildCommend(String baseCommend) {
        if (isWindows()) {
            return baseCommend + ".cmd";
        }
        return baseCommend;
    }

    /**
     * 判断当前系统是否为 windows
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     * @param dir
     * @param commend
     * @param timeoutSeconds
     * @return
     */
    public boolean executeCommend(File dir, String commend, int timeoutSeconds) {

        try {
            log.info("在目录 {} 中执行命令：{}", dir.getAbsolutePath(), commend);
            Process process = RuntimeUtil.exec(
                    null, dir, commend.split("\\s+")
            );
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.info("命令执行超时({}秒)，强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功：{}", commend);
                return true;
            } else {
                log.error("命令执行失败， 退出码：{}", exitCode);
                return false;
            }
        } catch (InterruptedException e) {
            log.error("执行命令失败：{}， 错误信息：{}", commend, e.getMessage());
            return false;
        }
    }
}