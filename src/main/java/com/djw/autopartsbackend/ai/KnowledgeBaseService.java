package com.djw.autopartsbackend.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库服务 - RAG检索增强生成
 * 支持文档导入、向量化和语义检索
 *
 * @author dengjiawen
 * @since 2026-04-01
 */
@Slf4j
@Service
public class KnowledgeBaseService {

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;
    private DocumentSplitter documentSplitter;

    /**
     * 初始化RAG组件
     */
    @PostConstruct
    public void init() {
        log.info("初始化RAG知识库服务...");
        
        // 使用本地ONNX模型进行向量化（无需调用外部API）
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        
        // 使用内存向量存储（生产环境可替换为Redis或Milvus）
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        
        // 文档分割器：每段最多300个token，重叠30个token
        this.documentSplitter = DocumentSplitters.recursive(300, 30);
        
        log.info("RAG知识库服务初始化完成");
    }

    /**
     * 导入文档到知识库
     *
     * @param content  文档内容
     * @param metadata 文档元数据（如来源、标题等）
     */
    public void importDocument(String content, Metadata metadata) {
        log.info("导入文档到知识库, 内容长度: {}", content.length());
        
        // 创建文档
        Document document = Document.from(content, metadata);
        
        // 分割文档
        List<TextSegment> segments = documentSplitter.split(document);
        
        // 向量化并存储
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        
        log.info("文档导入完成, 分割为{}个片段", segments.size());
    }

    /**
     * 导入文档到知识库（无元数据）
     *
     * @param content 文档内容
     */
    public void importDocument(String content) {
        importDocument(content, new Metadata());
    }

    /**
     * 语义检索相关文档
     *
     * @param query  查询文本
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度分数（0.0-1.0）
     * @return 相关文档片段列表
     */
    public List<String> search(String query, int maxResults, double minScore) {
        log.info("语义检索: query={}, maxResults={}, minScore={}", query, maxResults, minScore);
        
        // 将查询向量化
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        
        // 搜索相似向量
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        
        // 提取文本内容
        List<String> results = searchResult.matches().stream()
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.toList());
        
        log.info("检索完成, 找到{}个相关片段", results.size());
        return results;
    }

    /**
     * 语义检索相关文档（默认参数）
     *
     * @param query 查询文本
     * @return 相关文档片段列表
     */
    public List<String> search(String query) {
        return search(query, 5, 0.7);
    }

    /**
     * 检索并格式化为上下文
     * 用于RAG增强提示词
     *
     * @param query 查询文本
     * @return 格式化的上下文文本
     */
    public String searchAsContext(String query) {
        List<String> relevantDocs = search(query, 3, 0.6);
        
        if (relevantDocs.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("以下是相关知识库内容，供参考：\n\n");
        
        for (int i = 0; i < relevantDocs.size(); i++) {
            context.append("【参考文档").append(i + 1).append("】\n");
            context.append(relevantDocs.get(i)).append("\n\n");
        }
        
        return context.toString();
    }

    /**
     * 清空知识库
     */
    public void clear() {
        log.info("清空知识库");
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }
}
