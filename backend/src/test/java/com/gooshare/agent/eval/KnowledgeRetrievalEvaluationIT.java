package com.gooshare.agent.eval;

import com.gooshare.common.KnowledgeType;
import com.gooshare.service.KnowledgeSearchService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;


@Tag("integration")
@SpringBootTest(properties = {
        "gooshare.rag.rebuild-on-startup=false"
})
public class KnowledgeRetrievalEvaluationIT {

    @Autowired
    private KnowledgeSearchService knowledgeSearchService;

    /**
     * 物流元数据过滤
     */
    @Test
    void shouldRetrieveShippingKnowledge() {

        List<Document> results = knowledgeSearchService.search(
                "平台可以查询物流单号吗？",
                KnowledgeType.SHIPPING
        );

        assertFalse(
                results.isEmpty(),
                "没有任何召回知识"
        );

        boolean allFromShipping = results.stream()
                .allMatch(document ->
                        "shipping.md".equals(
                                document.getMetadata().get("source")
                        ));

        assertTrue(
                allFromShipping,
                "召回结果中出现了非 shipping.md 文档"
        );

        String recalledText = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        assertTrue(
                recalledText.contains("物流单号"),
                "召回内容缺少物流单号相关证据"
        );

    }

    /**
     * 校园交易元数据过滤
     */
    @Test
    void shouldRetrieveCampusTradeKnowledge() {
        List<Document> documents = knowledgeSearchService.search(
                "线下面交怎么避免被骗？",
                KnowledgeType.CAMPUS_TRADE
        );

        assertFalse(documents.isEmpty());

        assertTrue(documents.stream().allMatch(
                document ->
                        "campus-trade.md".equals(document.getMetadata().get("source"))
        ));

        String content = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        assertTrue(content.contains("公共"));

    }

    /**
     * 平台规则检索测试
     */
    @Test
    void shouldRetrievePlatformRulesKnowledge(){
        List<Document> documents = knowledgeSearchService.search(
                "加入购物车会锁定库存吗？",
                KnowledgeType.PLATFORM_RULES
        );

        assertFalse(documents.isEmpty());

        assertTrue(documents.stream()
                .allMatch(document ->
                        "platform-rules.md".equals(document.getMetadata().get("source")))
                        );

        String content = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        assertTrue(content.contains("锁定库存"));
    }

}
