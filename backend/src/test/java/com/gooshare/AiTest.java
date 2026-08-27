package com.gooshare;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
//import lombok.var;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest  // 启动完整的 Spring 容器
public class AiTest {
    @Autowired
    private ReactAgent reactAgent;

    @Test
    public void testAgent() throws GraphRunnerException {
        String question = "我想买一个500元以内的礼物送女朋友，有什么推荐？";
        var response = reactAgent.call(question);
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("C:\\Users\\76822\\Desktop\\GooShare\\src\\main\\resources\\knowledge");
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        System.out.println(ingestor);

        System.out.println("AI 回答：\n" + response.getText());
    }
}