package com.leo.leoaigenplatform.core.parser;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 17:57
 **/
public class MultiCodeParser implements CodeParser<MultiJsonStructure> {

    private static final Pattern HTML_PATTERN =
            Pattern.compile("```html\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_PATTERN =
            Pattern.compile("```css\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_PATTERN =
            Pattern.compile("```(javascript|js)\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @Override
    public MultiJsonStructure parseCode(String codeContent) {
        MultiJsonStructure result = new MultiJsonStructure();

        if (StrUtil.isBlank(codeContent)) {
            return result;
        }

        result.setHtmlCode(extract(codeContent, HTML_PATTERN));
        result.setCssCode(extract(codeContent, CSS_PATTERN));
        result.setJsCode(extractJs(codeContent));
        result.setDescription("AI 生成 HTML / CSS / JS（支持流式解析）");

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

    /**
     * 提取 JS（兼容 js / javascript）
     */
    private static String extractJs(String text) {
        Matcher matcher = JS_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return null;
    }
}
