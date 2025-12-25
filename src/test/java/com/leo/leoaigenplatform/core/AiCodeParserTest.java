package com.leo.leoaigenplatform.core;

import com.leo.leoaigenplatform.ai.jsonModel.HTMLJsonStructure;
import com.leo.leoaigenplatform.ai.jsonModel.MultiJsonStructure;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 15:20
 **/
@SpringBootTest
class AiCodeParserTest {

    @Test
    void parseHtml() {
    }

    @Test
    void testParseMulti_shouldExtractHtmlCssJsCorrectly() {
        // 模拟 AI 的完整输出（包含说明文字 + Markdown 代码块）
        String aiResponse = """
                下面是为你生成的单页网站代码。
                
                ```html
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Test Page</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>Hello World</h1>
                    <script src="script.js"></script>
                </body>
                </html>
                ```
                
                ```css
                body {
                    margin: 0;
                    font-family: Arial, sans-serif;
                }
                ```
                
                ```javascript
                document.addEventListener("DOMContentLoaded", () => {
                    console.log("Page Loaded");
                });
                ```
                
                希望你喜欢这个示例！
                """;

        MultiJsonStructure result = AiCodeParser.parseMulti(aiResponse);

        assertNotNull(result);

        // HTML
        assertNotNull(result.getHtmlCode());
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
        assertTrue(result.getHtmlCode().contains("<h1>Hello World</h1>"));

        // CSS
        assertNotNull(result.getCssCode());
        assertTrue(result.getCssCode().contains("body {"));

        // JS
        assertNotNull(result.getJsCode());
        assertTrue(result.getJsCode().contains("DOMContentLoaded"));

        // 描述
        assertNotNull(result.getDescription());
    }


    @Test
    void testParseHtml_shouldExtractOnlyHtml() {
        String aiResponse = """
                这是生成的 HTML 页面：
                
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Only HTML</title>
                </head>
                <body>
                    <p>This is a test</p>
                </body>
                </html>
                ```
                
                以上是完整代码。
                """;

        HTMLJsonStructure result = AiCodeParser.parseHtml(aiResponse);

        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertTrue(result.getHtmlCode().contains("<title>Only HTML</title>"));
        assertTrue(result.getHtmlCode().contains("<p>This is a test</p>"));
        assertNotNull(result.getDescription());
    }

}