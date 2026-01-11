package com.leo.leoaigenplatform.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.leo.leoaigenplatform.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2026-01-02 13:25
 **/
@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Tool("写入文件到指定路径")
    public String writeFile(@P("文件相对路径") String relativePath,
                     @P("写入内容") String content,
                     @ToolMemoryId Long appId) {

        try {
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                String projectName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.FILE_SAVE_ROOT_DIR, projectName);
                path = projectRoot.resolve(relativePath);
            }
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件：{}", path.toAbsolutePath());
            return "文件写入成功：" + relativePath;

        } catch (Exception e) {
            String errorMessage = "AI 写入文件失败, appId=" + appId + ", path=" + relativePath + ", errorMessage: " + e.getMessage();
            log.error(errorMessage,e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getToolCnName() {
        return "写入文件";
    }

    @Override
    public String getToolExecutionResult(JSONObject arguments) {
        String relativePath = arguments.getStr("relativePath");
        String content = arguments.getStr("content");
        String suffix = FileUtil.getSuffix(relativePath);
        return String.format("""
                        [工具调用] %s %s
                        ```%s
                        %s
                        ```
                        """, getToolCnName(), relativePath, suffix, content);

    }
}