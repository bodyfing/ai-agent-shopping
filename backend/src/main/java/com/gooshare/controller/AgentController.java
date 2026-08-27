package com.gooshare.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.gooshare.agent.dto.AgentChatRequest;
import com.gooshare.agent.dto.AgentChatResponse;
import com.gooshare.common.Result;
import com.gooshare.entity.User;
import com.gooshare.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AgentController {

    private final ReactAgent gooShareAgent;

    @PostMapping("/chat-v2")
    public Result chat(
            @RequestBody(required = false)
            AgentChatRequest request
    ){
        if(request == null
            || request.getMessage() == null
            || request.getMessage().trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        String conversationId = normalizeConversationId(request.getConversationId());

        User currentUser = UserHolder.getUser();

        if(currentUser == null || currentUser.getId() == null) {
            return Result.error("请先登录");
        }

        String threadId = buildThreadId(
                currentUser.getId(),
                conversationId
        );

        log.info(
                "Agent会话路由：userId={}, conversationId={}, threadId={}",
                currentUser.getId(),
                conversationId,
                threadId
        );

        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        try{
            AssistantMessage response = gooShareAgent.call(
                    request.getMessage().trim(),
                    config
            );

            String answer = response.getText();

            if(answer == null || answer.isBlank()){
                answer = "AI 助手暂时没有生成有效回答";
            }

            return Result.success(
                    new AgentChatResponse(
                            conversationId,
                            answer
                    )
            );
        } catch(GraphRunnerException e){
            return Result.error("AI 助手执行失败，请稍后重试");
        }
    }

    /**
     * 将用户和会话绑定，防止不同用户使用相同 conversationId 串话。
     */
    private String buildThreadId(Long userId, String conversationId) {
        return "user:"
                + userId
                + ":conversationId:"
                + conversationId;
    }

    private String normalizeConversationId(String conversationId) {
        if(conversationId == null || conversationId.trim().isEmpty()){
            return UUID.randomUUID().toString();
        }

        return conversationId.trim();
    }
}
