package com.leo.leoaigenplatform.model.dto.app;

import com.leo.leoaigenplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用查询请求（用户）
 *
 * @author Leo
 * @since 1.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（支持模糊查询）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}

