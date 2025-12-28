package com.leo.leoaigenplatform.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author Leo
 * @since 1.0.1
 */
@Getter
public enum MessageType {

    /**
     * 用户消息
     */
    USER("用户消息", "user"),

    /**
     * AI消息
     */
    AI("AI消息", "ai");

    /**
     * 类型描述
     */
    private final String text;

    /**
     * 类型值（用于数据库存储）
     */
    private final String value;

    MessageType(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 类型值
     * @return 消息类型枚举
     */
    public static MessageType getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getValue().equals(value)) {
                return messageType;
            }
        }
        return null;
    }
}
