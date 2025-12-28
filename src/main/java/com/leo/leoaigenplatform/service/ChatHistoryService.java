package com.leo.leoaigenplatform.service;

import com.leo.leoaigenplatform.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.entity.ChatHistory;
import com.leo.leoaigenplatform.model.vo.ChatHistoryVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author Leo
 * @since 1.0.1
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    
    /**
     * 添加对话消息
     *
     * @param appId 应用id
     * @param message 消息内容
     * @param messageType 消息类型：user/ai
     * @param userId 用户id
     * @param parentId 父消息id（可选，用于上下文关联）
     * @return 保存的对话历史id
     */
    Long addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId);

    Page<ChatHistoryVO> listAppChatHistoryByPage(Long appId, int pageSize,
                                                 LocalDateTime lastCreateTime,
                                                 LoginUser loginUser);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用id删除所有对话历史（应用删除时调用）
     *
     * @param appId 应用id
     * @return 是否成功
     */
    void deleteChatHistoryByAppId(Long appId);

    /**
     * 获取对话历史视图对象
     *
     * @param chatHistory 对话历史实体
     * @return 对话历史视图对象
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 获取对话历史视图对象列表
     *
     * @param chatHistoryList 对话历史实体列表
     * @return 对话历史视图对象列表
     */
    List<ChatHistoryVO> getChatHistoryVOList(List<ChatHistory> chatHistoryList);
}