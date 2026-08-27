package com.gooshare.agent.eval;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SpringBootTest(properties = {
        "gooshare.rag.rebuild-on-startup=false"
})
public class AgentMemoryIsolationEvaluationIT {

    @Autowired
    private ReactAgent gooShareAgent;
    @Test
    void shouldRememberWithinThreadAndIsolateDifferentUsers()
            throws Exception {

        String conversationId = UUID.randomUUID().toString();

        String memoryMarker = "预算码-"
                + UUID.randomUUID().toString().substring(0, 8);

        RunnableConfig userAConfig = RunnableConfig.builder()
                .threadId(
                        "user:900001:conversationId:" + conversationId
                )
                .build();

        RunnableConfig userBConfig = RunnableConfig.builder()
                .threadId(
                        "user:900002:conversationId:" + conversationId
                )
                .build();

        gooShareAgent.call(
                "请记住我的商品预算标识是"
                        + memoryMarker
                        + "，只回复已记住。",
                userAConfig
        );

        AssistantMessage userAResponse = gooShareAgent.call(
                "我刚才说的商品预算标识是什么？",
                userAConfig
        );

        AssistantMessage userBResponse = gooShareAgent.call(
                "我刚才说的商品预算标识是什么？",
                userBConfig
        );

        String userAAnswer = userAResponse.getText();
        String userBAnswer = userBResponse.getText();

        assertTrue(
                userAAnswer.contains(memoryMarker),
                "同一用户、同一会话没有恢复记忆，实际回答："
                        + userAAnswer
        );

        assertFalse(
                userBAnswer.contains(memoryMarker),
                "用户B读取到了用户A的记忆，发生跨用户污染："
                        + userBAnswer
        );
    }
    @Test
    void shouldIsolateDifferentConversationsOfSameUser()
            throws Exception {

        String userId = "900003";

        String memoryMarker = "偏好码-"
                + UUID.randomUUID().toString().substring(0, 8);

        RunnableConfig conversationAConfig = RunnableConfig.builder()
                .threadId(
                        "user:" + userId
                                + ":conversationId:"
                                + UUID.randomUUID()
                )
                .build();

        RunnableConfig conversationBConfig = RunnableConfig.builder()
                .threadId(
                        "user:" + userId
                                + ":conversationId:"
                                + UUID.randomUUID()
                )
                .build();

        gooShareAgent.call(
                "请记住我的商品偏好标识是"
                        + memoryMarker
                        + "，只回复已记住。",
                conversationAConfig
        );

        AssistantMessage conversationAResponse =
                gooShareAgent.call(
                        "我刚才说的商品偏好标识是什么？",
                        conversationAConfig
                );

        AssistantMessage conversationBResponse =
                gooShareAgent.call(
                        "我刚才说的商品偏好标识是什么？",
                        conversationBConfig
                );

        String conversationAAnswer =
                conversationAResponse.getText();

        String conversationBAnswer =
                conversationBResponse.getText();

        assertTrue(
                conversationAAnswer.contains(memoryMarker),
                "原会话没有恢复记忆，实际回答："
                        + conversationAAnswer
        );

        assertFalse(
                conversationBAnswer.contains(memoryMarker),
                "新会话读取到了旧会话记忆："
                        + conversationBAnswer
        );
    }


}