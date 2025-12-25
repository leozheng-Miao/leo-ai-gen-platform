package com.leo.leoaigenplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用添加请求
 *
 * @author Leo
 * @since 1.0.1
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}

