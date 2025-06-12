package com.chenjie.elasticsearch;

import com.chenjie.elasticsearch.vo.AnnouncementVo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
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

    private static final String INDEX_NAME = "test";
    private static final String DOCUMENT_ID = "1";

    @Test
    public void testElasticsearch() throws IOException {
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
