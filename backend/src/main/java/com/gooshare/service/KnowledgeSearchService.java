package com.gooshare.service;


import com.gooshare.common.KnowledgeType;
import org.springframework.ai.document.Document;

import java.util.List;

public interface KnowledgeSearchService {
    List<Document> search(String query, KnowledgeType type);

    /**
     * 不指定类型时查询全部知识。
     */
    default List<Document> search(String query){
        return search(query, KnowledgeType.ALL);
    }
}
