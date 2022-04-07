package com.brotherming.community.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.CommentMapper;
import com.brotherming.community.entity.Comment;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.service.CommentService;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostService discussPostService;

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Override
    public void addComment(Comment comment) {
        if (ObjectUtil.isEmpty(comment)) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //添加评论
        comment.setContent(HtmlUtil.escape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        commentMapper.insert(comment);
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST) {
            Integer count = new LambdaQueryChainWrapper<>(commentMapper).eq(Comment::getEntityType, comment.getEntityType())
                    .eq(Comment::getEntityId, comment.getEntityId()).count();
            discussPostService.lambdaUpdate().set(DiscussPost::getCommentCount,count)
                    .eq(DiscussPost::getId,comment.getEntityId()).update();
        }
    }
}
