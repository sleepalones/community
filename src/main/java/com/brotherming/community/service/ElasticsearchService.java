package com.brotherming.community.service;

import com.brotherming.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {

    void saveDiscussPost(DiscussPost post);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);

}
