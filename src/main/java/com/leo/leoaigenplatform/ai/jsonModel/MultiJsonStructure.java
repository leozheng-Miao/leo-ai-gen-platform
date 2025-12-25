package com.leo.leoaigenplatform.ai.jsonModel;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:27
 **/
@Data
@Description("生成HTML、CSS、JS文件的Json Schema")
public class MultiJsonStructure {
    @Description("生成的HTML代码")
    private String htmlCode;
    @Description("生成的CSS代码")
    private String cssCode;
    @Description("生成的JS代码")
    private String jsCode;
    @Description("生成代码的描述")
    private String description;
}
