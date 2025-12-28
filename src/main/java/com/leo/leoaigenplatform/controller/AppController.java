package com.leo.leoaigenplatform.controller;

import cn.hutool.json.JSONUtil;
import com.leo.leoaigenplatform.annotation.AuthCheck;
import com.leo.leoaigenplatform.common.BaseResponse;
import com.leo.leoaigenplatform.common.DeleteRequest;
import com.leo.leoaigenplatform.common.ResultUtils;
import com.leo.leoaigenplatform.constant.UserConstant;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.model.dto.app.*;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.vo.AppVO;
import com.leo.leoaigenplatform.service.AppService;
import com.leo.leoaigenplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author Leo
 * @since 1.0.1
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private UserService userService;

    /**
     * 应用聊天生成代码 - SSE
     * @param appId
     * @param userMessage
     * @param request
     * @return
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                    @RequestParam String userMessage,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userMessage == null || userMessage.isEmpty(), ErrorCode.PARAMS_ERROR);
        LoginUser loginUser = userService.getLoginUser(request);
        Flux<String> result = appService.chatToGEnCode(userMessage, appId, loginUser);
        return result.map(chunk -> {
            Map<String, String> wrapper = Map.of("d", chunk);
            String jsonStr = JSONUtil.toJsonStr(wrapper);
            return ServerSentEvent.<String>builder()
                    .data(jsonStr)
                    .build();
        })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    @PostMapping(value = "/app/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        String deployPath = appService.deployApp(appDeployRequest.getId(), userService.getLoginUser(request));
        return ResultUtils.success(deployPath);
    }

    /**
     * 【用户】创建应用（须填写 initPrompt）
     *
     * @param appAddRequest 应用添加请求
     * @param request HTTP请求
     * @return 应用id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = appService.addApp(appAddRequest, request);
        return ResultUtils.success(id);
    }

    /**
     * 【用户】根据 id 编辑自己的应用信息（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Boolean result = appService.updateApp(appUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Boolean result = appService.deleteApp(deleteRequest.getId(), request);
        return ResultUtils.success(result);
    }

    /**
     * 【用户】根据 id 查看自己的应用详情
     *
     * @param id 应用id
     * @param request HTTP请求
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppById(@RequestParam Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AppVO appVO = appService.getAppVOById(id, request);
        return ResultUtils.success(appVO);
    }

    /**
     * 【用户】分页查看自己的应用列表（支持根据名称查询，每页最多20个）
     *
     * @param appQueryRequest 应用查询请求
     * @param request HTTP请求
     * @return 分页结果
     */
    @PostMapping("/list/my/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> appVOPage = appService.listMyAppByPage(appQueryRequest, request);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【用户】分页查询精选应用列表（支持根据名称查询，每页最多20个）
     *
     * @param appQueryRequest 应用查询请求
     * @return 分页结果
     */
    @PostMapping("/list/featured/page/vo")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> appVOPage = appService.listFeaturedAppByPage(appQueryRequest);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 【管理员】根据 id 删除任意应用
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Boolean result = appService.adminDeleteApp(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 【管理员】根据 id 查询任意应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> adminGetAppById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AppVO appVO = appService.adminGetAppVOById(id);
        return ResultUtils.success(appVO);
    }

    /**
     * 【管理员】根据 id 更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appAdminUpdateRequest 管理员应用更新请求
     * @return 是否成功
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        Boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 【管理员】分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appAdminQueryRequest 管理员应用查询请求
     * @return 分页结果
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> adminListAppByPage(@RequestBody AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> appVOPage = appService.adminListAppByPage(appAdminQueryRequest);
        return ResultUtils.success(appVOPage);
    }

}
