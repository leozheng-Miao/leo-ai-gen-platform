package com.leo.leoaigenplatform.ai.jsonModel;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("应用名称结构")
public class AppNameJson {

    @Description("应用名称")
    private String appName;
}
