package com.brotherming.community;

import com.brotherming.community.dao.DiscussPostMapper;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.service.ElasticsearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.annotation.Resource;

@SpringBootTest
public class ElasticsearchTests {

    @Resource
    private ElasticsearchRestTemplate restTemplate;

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private ElasticsearchService elasticsearchService;

    @Test
    void testDelete() {
        /*NativeSearchQuery query = new NativeSearchQueryBuilder()
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
        }*/
        /*discussPostMapper.selectList(null).stream()
                .map(DiscussPost::getId).forEach(id -> elasticsearchService.deleteDiscussPost(id));*/
        //elasticsearchService.deleteDiscussPost(290);
    }

    @Test
    void testInsert() {
        for (DiscussPost post : discussPostMapper.selectList(null)) {
            elasticsearchService.saveDiscussPost(post);
        }
        /*DiscussPost post = discussPostMapper.selectById(290);
        elasticsearchService.saveDiscussPost(post);*/
    }

    @Test
    void testQuery() {
        Page<DiscussPost> discussPosts = elasticsearchService.searchDiscussPost("异常", 0, 10);
        System.out.println(discussPosts.getTotalElements());
        System.out.println(discussPosts.getTotalPages());
        System.out.println(discussPosts.getNumber());
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

}
