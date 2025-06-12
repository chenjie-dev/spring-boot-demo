package com.chenjie.elasticsearch;

import com.chenjie.elasticsearch.vo.AnnouncementVo;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

    private static final AnnouncementVo searchContent = new AnnouncementVo();

    static {
        searchContent.setType(2);
        searchContent.setAnnouncementcontent("茅台");
    }

    @Test
    public void testElasticsearch() throws IOException {

    }
}
