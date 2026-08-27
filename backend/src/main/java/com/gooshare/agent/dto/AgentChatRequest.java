package com.gooshare.agent.dto;

import lombok.Data;

@Data
public class AgentChatRequest {

    /**
     * 会话 ID
     * 第一次请求可以为空，后端会自动生成
     */
    private String conversationId;

    /**
     * 用户本次发送到消息
     */
    private String message;

}
