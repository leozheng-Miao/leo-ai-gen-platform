package com.leo.leoaigenplatform.util;

import com.leo.leoaigenplatform.model.entity.ChatHistory;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMarkdownConverter {

    public static String convert(Long appId, List<ChatHistory> historyList) {
        StringBuilder sb = new StringBuilder();

        sb.append("# 聊天记录导出\n\n");
        sb.append("- App ID: ").append(appId).append("\n");
        sb.append("- 导出时间: ").append(LocalDateTime.now()).append("\n\n");
        sb.append("---\n\n");

        for (ChatHistory history : historyList) {
            sb.append("## ")
              .append(history.getMessageType().equalsIgnoreCase("user") ? "🧑 用户" : "🤖 AI")
              .append("\n\n");

            sb.append("> ")
              .append(history.getMessage().replace("\n", "\n> "))
              .append("\n\n");

            sb.append("_")
              .append(history.getCreateTime())
              .append("_\n\n");

            sb.append("---\n\n");
        }

        return sb.toString();
    }
}