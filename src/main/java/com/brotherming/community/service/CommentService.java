package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.Comment;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
public interface CommentService extends IService<Comment> {

    void addComment(Comment comment);

    int findCommentCountByUserId(int userId);

    List<Comment> findNewCommentByUserId(int offset, int limit, int userId);

}
