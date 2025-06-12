package com.chenjie.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchClient client;

    private static final String INDEX_NAME = "test_index";
    private static final String DOCUMENT_ID = "1";

    @Test
    public void testElasticsearch() throws IOException {
        // 检查索引是否存在
        boolean indexExists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        
        if (!indexExists) {
            // 创建索引
            CreateIndexResponse createIndexResponse = client.indices().create(c -> c
                .index(INDEX_NAME)
                .mappings(m -> m
                    .properties("title", p -> p.text(t -> t))
                    .properties("content", p -> p.text(t -> t))
                )
            );
            log.info("索引创建成功: {}", createIndexResponse.acknowledged());
        }

        try {
            // 尝试获取文档
            GetResponse<Object> response = client.get(g -> g
                .index(INDEX_NAME)
                .id(DOCUMENT_ID),
                Object.class
            );

            if (response.found()) {
                log.info("文档已存在: {}", response.source());
            } else {
                // 创建新文档
                IndexResponse indexResponse = client.index(i -> i
                    .index(INDEX_NAME)
                    .id(DOCUMENT_ID)
                    .document(new TestDocument("test", "This is a test document"))
                );
                
                log.info("文档创建成功: {}", indexResponse.result());
            }
        } catch (Exception e) {
            log.error("操作失败: {}", e.getMessage(), e);
        }
    }
}

// 测试文档类
class TestDocument {
    private String title;
    private String content;

    public TestDocument() {
    }

    public TestDocument(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
