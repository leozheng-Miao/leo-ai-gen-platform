package com.leo.leoaigenplatform.model.dto.chatHistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话历史添加请求
 *
 * @author Leo
 * @since 1.0.1
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型：user/ai
     */
    private String messageType;

    /**
     * 父消息id（用于上下文关联，可选）
     */
    private Long parentId;

    private static final long serialVersionUID = 1L;

}