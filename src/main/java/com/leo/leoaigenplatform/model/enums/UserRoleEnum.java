package com.leo.leoaigenplatform.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-22 10:49
 **/
@Getter
public enum UserRoleEnum {

    ADMIN("管理员", "admin", 10),
    VIP("会员用户", "vip", 2),
    USER("普通用户", "user", 0);

    private final String text;
    private final String value;
    /**
     * 权限等级值
     * 值越大 权限越高
     */
    private final int level;

    UserRoleEnum(String text, String value, int level) {
        this.text = text;
        this.value = value;
        this.level = level;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.getValue().equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }

    public boolean hasPermission(UserRoleEnum required) {
        return this.level >= required.level;
    }

}
