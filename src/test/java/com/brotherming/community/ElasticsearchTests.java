package com.brotherming.community;

import com.brotherming.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class ElasticsearchTests {

    @Resource
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void testDelete() {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSorts(
                        SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC)
                ).withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> search = restTemplate.search(query, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();

        for (SearchHit<DiscussPost> searchHit : searchHits) {
            List<String> titleList = searchHit.getHighlightField("title");
            List<String> contentList = searchHit.getHighlightField("content");
            DiscussPost content = searchHit.getContent();
            System.out.println();
        }

    }


}
