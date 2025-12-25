package com.leo.leoaigenplatform.core.parser;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: leo-ai-gen-platform
 * @description: 解析 html 单个页面代码
 * @author: Miao Zheng
 * @date: 2025-12-23 17:55
 **/
public class HTMLCodeParser implements CodeParser<HTMLJsonStructure> {

    private static final Pattern HTML_PATTERN =
            Pattern.compile("```html\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * @param codeContent
     * @return
     */
    @Override
    public HTMLJsonStructure parseCode(String codeContent) {
        HTMLJsonStructure result = new HTMLJsonStructure();

        if (StrUtil.isBlank(codeContent)) {
            return result;
        }

        result.setHtmlCode(extract(codeContent, HTML_PATTERN));
        result.setDescription("AI 生成 HTML（支持流式解析）");

        return result;
    }

    /**
     * 提取普通代码块
     */
    private static String extract(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
