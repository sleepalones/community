package com.brotherming.community.service.impl;

import com.brotherming.community.entity.Comment;
import com.brotherming.community.dao.CommentMapper;
import com.brotherming.community.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
