package com.gooshare.service.impl;

import com.gooshare.common.KnowledgeType;
import com.gooshare.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private final VectorStore vectorStore;

    public List<Document> search(String query, KnowledgeType knowledgeType) {
        if(!StringUtils.hasText(query)){
            throw new IllegalArgumentException("知识查询内容不能为空");
        }

        KnowledgeType safeType = knowledgeType == null ? KnowledgeType.ALL : knowledgeType;

        var requestBuilder = SearchRequest.builder()
                .query(query.trim())
                .topK(4);

        if(safeType != KnowledgeType.ALL){
            requestBuilder.filterExpression(
                    "knowledgeType == '"
                    +safeType.getMetadataValue()
                    +"'"
            );
        }

        SearchRequest request = requestBuilder.build();

//        return vectorStore.similaritySearch(request);
        List<Document> results =
                vectorStore.similaritySearch(request);
        log.info(
                "知识检索条件：query={}, knowledgeType={}",
                query,
                safeType
        );

        for(int i = 0; i < results.size(); i++){
            Document document = results.get(i);
            log.info(
                    "知识召回：rank={}, score={}, source={}, chunkIndex={}",
                    i + 1,
                    document.getScore(),
                    document.getMetadata().get("source"),
                    document.getMetadata().get("chunkIndex")
            );
        }
        return results;
    }

}
