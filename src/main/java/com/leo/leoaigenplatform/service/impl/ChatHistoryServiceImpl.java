package com.leo.leoaigenplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.constant.UserConstant;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.mapper.ChatHistoryMapper;
import com.leo.leoaigenplatform.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.entity.App;
import com.leo.leoaigenplatform.model.entity.ChatHistory;
import com.leo.leoaigenplatform.model.enums.MessageType;
import com.leo.leoaigenplatform.model.vo.ChatHistoryVO;
import com.leo.leoaigenplatform.service.AppService;
import com.leo.leoaigenplatform.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author Leo
 * @since 1.0.1
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Lazy
    @Resource
    private AppService appService;

    @Override
    public Long addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 验证消息类型是否有效
        MessageType messageTypeEnum = MessageType.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .parentId(parentId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        boolean save = this.save(chatHistory);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存对话消息失败");

        return chatHistory.getId();
    }

    @Override
    public Page<ChatHistoryVO> listAppChatHistoryByPage(Long appId, int pageSize,
                                                        LocalDateTime lastCreateTime,
                                                        LoginUser loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        Page<ChatHistory> chatHistoryPage = this.page(Page.of(1, pageSize), queryWrapper);
        List<ChatHistory> chatHistoryList = chatHistoryPage.getRecords();

        // 转换为VO
        List<ChatHistoryVO> chatHistoryVOList = getChatHistoryVOList(chatHistoryList);

        // 构建分页结果（这里使用简化的分页，因为我们使用的是游标分页）
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(1, pageSize, chatHistoryList.size());
        chatHistoryVOPage.setRecords(chatHistoryVOList);

        return chatHistoryVOPage;
    }


    /**
     * 加载聊天历史到内存中
     *
     * @param appId      应用ID
     * @param chatMemory 聊天记忆窗口对象
     * @param maxCount   最大加载条数
     * @return 实际加载的条数，异常时返回0
     */
    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 构造查询条件，按创建时间倒序排列，并限制查询数量
            QueryWrapper queryWrapper = new QueryWrapper()
                    .eq(ChatHistory::getAppId, appId)  // 匹配指定应用ID
                    .orderBy(ChatHistory::getCreateTime, false)  // 按创建时间降序排列
                    .limit(1, maxCount);  // 设置查询范围，跳过最新的一条，加载maxCount条记录
            // 执行查询获取聊天历史列表
            List<ChatHistory> historyList = this.list(queryWrapper);
            // 如果历史记录为空，直接返回0
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            //反转列表， 确保时间正序
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            //先清理里是缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (history.getMessageType().equals(MessageType.USER.getValue())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                } else if (history.getMessageType().equals(MessageType.AI.getValue())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                }
                loadedCount++;
            }
            log.info("成功加载 {} 条对话历史到 应用 {} 中", loadedCount, appId);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载对话历史到应用 - {} 内存时发生异常, error: {}", appId, e.getMessage());
            return 0;
        }
    }

    /**
     * 导入聊天记录
     *
     * @param appId
     * @param loginUser
     * @param exportPath
     * @return
     */
    @Override
    public boolean exportChatHistory(Long appId, LoginUser loginUser, String exportPath) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR, "用户不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(exportPath), ErrorCode.PARAMS_ERROR, "导出路径不能为空");

        // 权限校验
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权导出该应用的对话历史");

        // 查找聊天记录 - 按照时间正序
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, true);


        List<ChatHistory> chatHistoryList = this.list(queryWrapper);
        if (CollUtil.isEmpty(chatHistoryList)) {
            log.warn("应用 {} 没有聊天记录,跳过导出", appId);
            return false;
        }

        // 生成 Markdown 内容
        StringBuilder md = new StringBuilder();
        md.append("# Chat History\n\n");
        md.append("- AppId: ").append(appId).append("\n");
        md.append("- Export Time: ").append(LocalDateTime.now()).append("\n\n");

        for (ChatHistory history : chatHistoryList) {
            String role = MessageType.USER.getValue().equals(history.getMessageType())
                    ? "👤 User"
                    : "🤖 AI";

            md.append("## ").append(role)
                    .append(" | ")
                    .append(history.getCreateTime())
                    .append("\n\n");

            md.append(history.getMessage()).append("\n\n");
        }

        // 处理导出路径
        try {
            File targetFile;
            if (exportPath.endsWith(".md")) {
                targetFile = new File(exportPath);
            } else {
                File dir = new File(exportPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                targetFile = new File(dir, "chat-history-app-" + appId + ".md");
            }

            cn.hutool.core.io.FileUtil.writeUtf8String(md.toString(), targetFile);
            log.info("成功导出聊天记录，appId={}, path={}", appId, targetFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            log.error("导出聊天记录失败，appId={}, error={}", appId, e.getMessage(), e);
            return false;
        }

    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }


    @Override
    public void deleteChatHistoryByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("appId", appId);

        this.remove(queryWrapper);
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        return chatHistoryVO;
    }

    @Override
    public List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList) {
        if (CollUtil.isEmpty(chatHistoryList)) {
            return new ArrayList<>();
        }
        return chatHistoryList.stream()
                .map(this::getChatHistoryVO)
                .collect(java.util.stream.Collectors.toList());
    }
}