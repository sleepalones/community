package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
public interface DiscussPostService extends IService<DiscussPost> {


    void addDiscussPost(DiscussPost post);

    List<DiscussPost> findDiscussPostPage(PageInfo pageInfo, int orderMode);

    void init();

    int findDiscussPostTotal(int orderMode);
}
