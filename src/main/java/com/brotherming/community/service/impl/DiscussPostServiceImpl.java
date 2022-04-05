package com.brotherming.community.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.DiscussPostMapper;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Override
    public void addDiscussPost(DiscussPost post) {
        if (ObjectUtil.isEmpty(post)) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //转义HTML标记
        post.setTitle(HtmlUtil.escape(post.getTitle()));
        post.setContent(HtmlUtil.escape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        post.setType(0);
        post.setStatus(0);
        post.setCommentCount(0);
        post.setScore(0.0);
        discussPostMapper.insert(post);
    }
}
