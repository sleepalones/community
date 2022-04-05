package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.DiscussPost;

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
}
