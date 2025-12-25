package com.leo.leoaigenplatform.ai.jsonModel;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-23 13:26
 **/
@Data
@Description("生成HTML文件的Json Schema")
public class HTMLJsonStructure {


    @Description("生成的HTML代码")
    private String htmlCode;
    @Description("生成代码的描述")
    private String description;


}
