package com.brotherming.community.service.impl;

import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.service.ElasticsearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void saveDiscussPost(DiscussPost post) {
        elasticsearchRestTemplate.save(post);
    }

    @Override
    public void deleteDiscussPost(int id) {
        elasticsearchRestTemplate.delete(String.valueOf(id),DiscussPost.class);
    }

    @Override
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        PageRequest pageRequest = PageRequest.of(current,limit);
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(
                        SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC)
                ).withPageable(pageRequest)
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(query, DiscussPost.class);
        List<DiscussPost> discussPosts = new ArrayList<>();
        search.forEach(item -> {
            DiscussPost post = item.getContent();
            List<String> titleList = item.getHighlightField("title");
            if (titleList.size() > 0) {
                post.setTitle(titleList.get(0));
            }
            List<String> contentList = item.getHighlightField("content");
            if (contentList.size() > 0) {
                post.setContent(contentList.get(0));
            }
            discussPosts.add(post);
        });
        return new PageImpl<>(discussPosts,pageRequest,search.getTotalHits());
    }
}
