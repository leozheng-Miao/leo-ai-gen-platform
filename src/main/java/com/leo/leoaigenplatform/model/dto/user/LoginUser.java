package com.leo.leoaigenplatform.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @program: leo-ai-gen-platform
 * @description: 存入 session 中， 只用作 当前登陆用户 的 权限判断
 * @author: Miao Zheng
 * @date: 2025-12-22 12:17
 **/
@Data
public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id（核心）
     */
    private Long id;

    /**
     * 用户角色（权限 / 会员判断）
     */
    private String userRole;
}
