package com.gooshare.agent.tool;

import com.gooshare.agent.dto.KnowledgeSearchToolRequest;
import com.gooshare.common.KnowledgeType;
import com.gooshare.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiKnowledgeTools {

    private final KnowledgeSearchService knowledgeSearchService;

    @Tool(description = """
        查询 GooShare 校园二手交易平台的规则知识。

        当用户查询平台规则、校园交易规范、交易安全、
        禁止交易物品、发货、收货、物流或纠纷处理时调用。

        回答必须依据本工具返回的知识片段，
        不得编造平台不存在的规则。
        """)
    public List<Document> searchPlatformKnowledge(
            @ToolParam(description = "平台知识查询条件")
            KnowledgeSearchToolRequest input
    ) {
        if (input == null) {
            throw new IllegalArgumentException("知识查询条件不能为空");
        }

        KnowledgeType knowledgeType =
                input.getKnowledgeType() == null
                        ? KnowledgeType.ALL
                        : input.getKnowledgeType();

        List<Document> documents =
                knowledgeSearchService.search(
                        input.getQuery(),
                        knowledgeType
                );


        log.info(
                "Agent知识检索：query={}, knowledgeType={}, resultCount={}",
                input.getQuery(),
                knowledgeType,
                documents.size()
        );

        return documents;
    }
}
