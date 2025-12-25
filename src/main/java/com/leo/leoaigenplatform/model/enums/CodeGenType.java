package com.leo.leoaigenplatform.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * AI 代码生成类型枚举
 * <p>
 * 用于门面模式中区分不同生成策略
 */
@Getter
public enum CodeGenType {

    /**
     * 单 HTML 文件
     */
    HTML(
            "html",
            "生成单个 HTML 文件"
    ),

    /**
     * HTML + CSS + JS 多文件
     */
    MULTI_FILE(
            "multi_file",
            "生成 HTML + CSS + JS 文件"
    );

    /**
     * 类型标识（可用于前端 / DB / 日志）
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    CodeGenType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 反查枚举（可选，但很实用）
     */
    public static CodeGenType of(String code) {
        if (ObjUtil.isEmpty(code)) {
            return null;
        }
        for (CodeGenType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported CodeGenType: " + code);
    }
}
