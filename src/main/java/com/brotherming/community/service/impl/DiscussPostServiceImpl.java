package com.brotherming.community.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.DiscussPostMapper;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostMapper discussPostMapper;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    private LoadingCache<String,List<DiscussPost>> postListCache;

    private LoadingCache<String,Integer> postRowsCache;


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

    @PostConstruct
    public void init() {
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize).expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    if (key == null || key.length() == 0) {
                        throw new IllegalArgumentException("参数错误");
                    }
                    String[] split = key.split(":");
                    if (split == null || split.length != 2) {
                        throw new IllegalArgumentException("参数错误");
                    }
                    Page<DiscussPost> page = new Page<>(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
                    LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
                    wrapper.ne(DiscussPost::getStatus,2)
                            .orderByDesc(DiscussPost::getType)
                            .orderByDesc(DiscussPost::getScore)
                            .orderByDesc(DiscussPost::getCreateTime);
                    logger.info("postListCache list db!");
                    return discussPostMapper.selectPage(page, wrapper).getRecords();
                });
        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder().maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(key -> {
                    logger.info("postRowsCache total db!");
                    return discussPostMapper.selectCount(null);
                });
    }

    @Override
    public List<DiscussPost> findDiscussPostPage(PageInfo pageInfo, int orderMode) {
        Page<DiscussPost> page = new Page<>(pageInfo.getCurrent(),pageInfo.getLimit());
        if (pageInfo.getCurrent() == 1 && orderMode == 1) {
            return postListCache.get(pageInfo.getCurrent() + ":" + pageInfo.getLimit());
        }
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        // 查询没有被拉黑的帖子
        wrapper.ne(DiscussPost::getStatus,2);
        wrapper.orderByDesc(DiscussPost::getType);
        if (orderMode == 1) {
            wrapper.orderByDesc(DiscussPost::getScore);
        }
        wrapper.orderByDesc(DiscussPost::getCreateTime);
        logger.info("list db!");
        Page<DiscussPost> discussPostPage = discussPostMapper.selectPage(page, wrapper);
        return discussPostPage.getRecords();
    }

    @Override
    public int findDiscussPostTotal(int orderMode) {
        if (orderMode == 1) {
            return postRowsCache.get("total");
        }
        logger.info("total db!");
        return discussPostMapper.selectCount(null);
    }
}
