package com.gooshare.config;

import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

//    // 1. 配置嵌入模型（使用本地免费模型，用于开发测试）
//    @Bean
//    public EmbeddingModel embeddingModel() {
//        return new OnnxEmbeddingModel();
//    }
//
//    // 2. 配置向量存储（使用内存存储，适合开发和测试）
//    @Bean
//    public EmbeddingStore embeddingStore() {
//        return new InMemoryEmbeddingStore
//    }
}
