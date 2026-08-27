package com.gooshare.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentChatResponse {

    /**
     * 前端后端请求必须继续携带这个 ID
     */
    private String conversationId;

    /**
     * Agent 最终回答
     */
    private String answer;
}
