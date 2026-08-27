package com.gooshare.agent.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "gooshare.rag",
        name = "rebuild-on-startup",
        havingValue = "true"
)
public class KnowledgeIngestionRunner implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    public void run(ApplicationArguments args) throws Exception{
        Resource[] resources = resourcePatternResolver.getResources(
                "classpath*:knowledge/*.md"
        );

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        int totalCount = 0;
        for (Resource resource : resources) {
            String fileName = Objects.requireNonNull(resource.getFilename());
            String text = resource.getContentAsString(StandardCharsets.UTF_8);

            Document originalDocument = Document.builder()
                    .text(text)
                    .metadata("source",fileName)
                    .metadata("knowledgeType", getKnowledgeType(fileName))
                    .build();

            List<Document> chunks = splitter.apply(
                    List.of(originalDocument)
            );

            List<Document> documents = IntStream.range(0, chunks.size())
                    .mapToObj(
                            index -> buildChunkDocument(
                                    fileName,
                                    index,
                                    chunks.get(index)
                            )
                    )
                    .toList();

            // 先删除该文件上一次导入的片段，防止重复入库
            vectorStore.delete("source == '" + fileName + "'");

            // 自动调用 EmbeddingModel，然后把文本、向量和元数据写入 Milvus
            vectorStore.add(documents);

            totalCount += documents.size();

            log.info(
                    "知识文件导入完成：source={}. chunkCount={}",
                    fileName,
                    documents.size()
            );
        }
        log.info(
                "知识库重建完成：fileCount={}, totalChunkCount={}",
                resources.length,
                totalCount
        );
    }

    private Document buildChunkDocument(
            String fileName,
            int chunkIndex,
            Document chunk
    ){
        String documentId = UUID.nameUUIDFromBytes(
                (fileName + ":" +chunkIndex)
                        .getBytes(StandardCharsets.UTF_8)
        ).toString();

        return Document.builder()
                .id(documentId)
                .text(chunk.getText())
                .metadata(chunk.getMetadata())
                .metadata("chunkIndex",chunkIndex)
                .build();
    }

    private String getKnowledgeType(String fileName) {
        return switch(fileName){
            case "platform-rules.md" -> "platform_rules";
            case "campus-trade.md" -> "campus_trade";
            case "shipping.md" -> "shipping";
            default -> "other";
        };
    }
}
