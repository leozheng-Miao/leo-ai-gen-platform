package com.leo.leoaigenplatform.core;

import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 代码解析器
 * <p>
 * 用途：
 * - 从 AI 返回的大段文本（包括流式拼接后的文本）中
 * - 解析出 Markdown 代码块里的 HTML / CSS / JS
 * <p>
 * 适配 Prompt 格式：
 * ```html
 * ...
 * ```
 * <p>
 * ```css
 * ...
 * ```
 * <p>
 * ```javascript
 * ...
 * ```
 */
@Deprecated
public class AiCodeParser {

    private static final Pattern HTML_PATTERN =
            Pattern.compile("```html\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_PATTERN =
            Pattern.compile("```css\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_PATTERN =
            Pattern.compile("```(javascript|js)\\s*(.*?)\\s*```",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private AiCodeParser() {
    }

    /**
     * 解析 HTML 单文件结构
     */
    public static HTMLJsonStructure parseHtml(String aiText) {
        HTMLJsonStructure result = new HTMLJsonStructure();

        if (StrUtil.isBlank(aiText)) {
            return result;
        }

        result.setHtmlCode(extract(aiText, HTML_PATTERN));
        result.setDescription("AI 生成 HTML（支持流式解析）");

        return result;
    }

    /**
     * 解析 HTML + CSS + JS 多文件结构
     */
    public static MultiJsonStructure parseMulti(String aiText) {
        MultiJsonStructure result = new MultiJsonStructure();

        if (StrUtil.isBlank(aiText)) {
            return result;
        }

        result.setHtmlCode(extract(aiText, HTML_PATTERN));
        result.setCssCode(extract(aiText, CSS_PATTERN));
        result.setJsCode(extractJs(aiText));
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
