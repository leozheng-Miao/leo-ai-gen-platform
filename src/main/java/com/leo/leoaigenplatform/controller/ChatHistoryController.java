package com.leo.leoaigenplatform.controller;

import com.leo.leoaigenplatform.annotation.AuthCheck;
import com.leo.leoaigenplatform.common.BaseResponse;
import com.leo.leoaigenplatform.common.ResultUtils;
import com.leo.leoaigenplatform.constant.UserConstant;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.model.dto.chatHistory.ChatHistoryAddRequest;
import com.leo.leoaigenplatform.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.entity.ChatHistory;
import com.leo.leoaigenplatform.model.vo.ChatHistoryVO;
import com.leo.leoaigenplatform.service.ChatHistoryService;
import com.leo.leoaigenplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author Leo
 * @since 1.0.1
 */
@RestController
@RequestMapping("/chatHistory")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 【用户】添加对话消息
     *
     * @param chatHistoryAddRequest 对话历史添加请求（需包含messageType字段：user/ai）
     * @param request HTTP请求
     * @return 保存的对话历史id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChatMessage(@RequestBody ChatHistoryAddRequest chatHistoryAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long loginUserId = userService.getLoginUser(request).getId();
        Long id = chatHistoryService.addChatMessage(
                chatHistoryAddRequest.getAppId(),
                chatHistoryAddRequest.getMessage(),
                chatHistoryAddRequest.getMessageType(),
                loginUserId,
                chatHistoryAddRequest.getParentId()
        );
        return ResultUtils.success(id);
    }

    /**
     * 【用户】分页查询某个应用的对话历史（仅应用创建者可见）
     * 每次加载最新10条消息，支持向前加载更多历史记录（基于游标）
     * @param appId 查询的 app id
     * @param pageSize 每页大小 默认 10
     * @param lastCreateTime 最新时间游标
     * @param request HTTP 请求
     * @return 分页结果VO
     */
    @PostMapping("/app/list/vo/{appId}")
    public BaseResponse<Page<ChatHistoryVO>> listChatHistoryByPage(@PathVariable Long appId,
                                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                                   @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                   HttpServletRequest request) {
        LoginUser loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录，无法查看历史记录");
        Page<ChatHistoryVO> chatHistoryVOPage = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(chatHistoryVOPage);
    }

    /**
     * 【管理员】分页查询所有应用的对话历史（按时间降序排序）
     *
     * @param chatHistoryAdminQueryRequest 管理员查询请求
     * @return 分页结果
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> adminListChatHistoryByPage(@RequestBody ChatHistoryQueryRequest chatHistoryAdminQueryRequest) {
        ThrowUtils.throwIf(chatHistoryAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageSize = chatHistoryAdminQueryRequest.getPageSize();
        int pageNum = chatHistoryAdminQueryRequest.getPageNum();
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryAdminQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 【用户】导出某个应用的对话历史到指定路径 （仅应用创建者和管理员可用）
     * @param appId
     * @param exportPath 需要导出的路径
     * @param request
     * @return
     */
    @PostMapping("/export/markdown")
    public BaseResponse<Boolean> exportChatHistory(@RequestParam Long appId,
                                                   @RequestParam String exportPath,
                                                   HttpServletRequest request) {
        LoginUser loginUser = userService.getLoginUser(request);
        boolean result = chatHistoryService.exportChatHistory(appId, loginUser, exportPath);
        return ResultUtils.success(result);

    }

}