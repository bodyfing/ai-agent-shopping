package com.gooshare.agent.eval;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.junit.jupiter.api.Tag;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Tag("integration")
@SpringBootTest(properties = {
        "gooshare.rag.rebuild-on-startup=false"
})
public class AgentAnswerEvaluationIT{

    @Autowired
    private ReactAgent gooShareAgent;

    @Test
    void shouldNotInventPublishingFee() throws Exception{
        RunnableConfig config = RunnableConfig.builder()
                .threadId("eval:publishing-fee:" + UUID.randomUUID())
                .build();

        AssistantMessage response = gooShareAgent.call(
                "平台发布商品要收多少手续费？",
                config
        );

        String answer = response.getText();

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        assertTrue(
                answer.contains("没有明确")||
                        answer.contains("无法确认"),
                "Agent没有正确表达知识库未说明，实际回答：" + answer
        );

        assertTrue(
                answer.contains("platform-rules.md"),
                "回答没有标注知识来源，实际回答" + answer
        );

        assertFalse(
                answer.contains("不收取手续费") ||
                        answer.contains("免费发布") ||
                        answer.contains("发布商品是免费的"),
                "Agent编造了免费发布揭露你，实际回答：" + answer
        );
    }

    @Test
    void shouldAnswerUnsupportedLogisticsCorrectly() throws Exception {
        RunnableConfig config = RunnableConfig.builder()
                .threadId("eval:logistics:" + UUID.randomUUID())
                .build();

        AssistantMessage response = gooShareAgent.call(
                "平台可以查询物流单号吗？",
                config
        );

        String answer = response.getText();

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        assertTrue(
                answer.contains("无法")
                        || answer.contains("尚未")
                        || answer.contains("不支持"),
                "Agent没有说明当前无法查询物流，实际回答：" + answer
        );

        assertTrue(
                answer.contains("shipping.md"),
                "回答没有标注物流知识来源，实际回答：" + answer
        );

        assertFalse(
                answer.contains("已经接入物流")
                        || answer.contains("已支持物流查询"),
                "Agent错误声称平台已经支持物流查询，实际回答：" + answer
        );
    }
    @Test
    void shouldAnswerSupportedCartRuleCorrectly() throws Exception {
        RunnableConfig config = RunnableConfig.builder()
                .threadId("eval:cart:" + UUID.randomUUID())
                .build();

        AssistantMessage response = gooShareAgent.call(
                "登录用户可以把商品加入购物车吗？",
                config
        );

        String answer = response.getText();

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        assertTrue(
                answer.contains("可以")
                        && answer.contains("登录"),
                "Agent没有正确说明购物车规则，实际回答：" + answer
        );

        assertTrue(
                answer.contains("platform-rules.md"),
                "回答没有标注平台规则来源，实际回答：" + answer
        );

        assertFalse(
                answer.contains("不支持购物车")
                        || answer.contains("无法加入购物车"),
                "Agent错误声称平台不支持购物车，实际回答：" + answer
        );
    }

    static Stream<AgentAnswerEvalCase> answerCases(){
        return Stream.of(
                new AgentAnswerEvalCase(
                        "手续费规则",
                        "平台发布商品要收多少手续费？",
                        List.of("没有明确","无法确认"),
                        List.of("免费发布","不收取手续费"),
                        "platform-rules.md"
                ),
                new AgentAnswerEvalCase(
                        "物流查询",
                        "平台可以查询物流单号吗？",
                        List.of("无法","尚未","不支持"),
                        List.of("已经接入物流","已支持物流查询"),
                        "shipping.md"
                ),
                new AgentAnswerEvalCase(
                        "购物车功能",
                        "登录用户可以把商品加入购物车吗？",
                        List.of("可以"),
                        List.of("不支持购物车","无法加入购物车"),
                        "platform-rules.md"
                ),
                new AgentAnswerEvalCase(
                        "面交地点规则",
                        "线下面交必须在图书馆进行吗？",
                        List.of("不强制", "不是必须"),
                        List.of("教学楼大厅", "食堂门口", "校内广场"),
                        "campus-trade.md"
                ),

                new AgentAnswerEvalCase(
                        "购物车库存规则",
                        "加入购物车以后会自动锁定库存吗？",
                        List.of(
                                "不会自动锁定库存",
                                "不会锁定库存",
                                "不锁定库存",
                                "不等于锁定库存"
                        ),
                        List.of(
                                "加入购物车会自动锁定库存",
                                "加入购物车后会锁定库存",
                                "加入购物车即可锁定库存"
                        ),
                        "platform-rules.md"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("answerCases")
    void shouldPassAnswerEvaluation(
            AgentAnswerEvalCase evalCase
    ) throws Exception {

        RunnableConfig config = RunnableConfig.builder()
                .threadId("eval:answer:" + UUID.randomUUID())
                .build();

        AssistantMessage response = gooShareAgent.call(
                evalCase.question(),
                config
        );

        String answer = response.getText();

        assertNotNull(
                answer,
                evalCase.caseName() + "：Agent回答为null"
        );

        assertFalse(
                answer.isBlank(),
                evalCase.caseName() + "：Agent回答为空"
        );

        boolean containsExpectedKeyword =
                evalCase.expectedKeywords().stream()
                        .anyMatch(answer::contains);

        assertTrue(
                containsExpectedKeyword,
                evalCase.caseName()
                        + "：没有命中预期关键词，实际回答："
                        + answer
        );

        assertTrue(
                answer.contains(evalCase.expectedSource()),
                evalCase.caseName()
                        + "：没有引用预期来源，实际回答："
                        + answer
        );

        boolean containsForbiddenKeyword =
                evalCase.forbiddenKeywords().stream()
                        .anyMatch(answer::contains);

        assertFalse(
                containsForbiddenKeyword,
                evalCase.caseName()
                        + "：命中了禁止关键词，实际回答："
                        + answer
        );
    }
}
