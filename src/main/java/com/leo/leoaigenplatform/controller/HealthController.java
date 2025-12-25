package com.leo.leoaigenplatform.controller;

import com.leo.leoaigenplatform.common.BaseResponse;
import com.leo.leoaigenplatform.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: leo-ai-gen-platform
 * @description:
 * @author: Miao Zheng
 * @date: 2025-12-21 14:18
 **/
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> check() {
        return ResultUtils.success("ok");
    }
}
