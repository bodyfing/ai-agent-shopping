package com.gooshare.agent.eval;

import java.util.List;

/**
 * 一条 Agent 回答评测用例
 *
 * @param caseName              用例名称
 * @param question              向 Agent 提出的问题
 * @param expectedKeywords      回答至少包含其中一个关键词
 * @param forbiddenKeywords     回答不应该包含的内容
 * @param expectedSource        预期引用的知识来源
 */
public record AgentAnswerEvalCase(
    String caseName,
    String question,
    List<String> expectedKeywords,
    List<String> forbiddenKeywords,
    String expectedSource
){

}
