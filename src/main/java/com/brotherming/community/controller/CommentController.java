package com.brotherming.community.controller;


import cn.hutool.core.date.DateUtil;
import com.brotherming.community.entity.Comment;
import com.brotherming.community.service.CommentService;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private HostHolder hostHolder;

    @PostMapping("/add/{discussPostId}")
    private String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setTargetId(comment.getTargetId()==null?0:comment.getTargetId());
        comment.setCreateTime(DateUtil.toLocalDateTime(new Date()));
        commentService.addComment(comment);
        return "redirect:/discussPost/detail/" + discussPostId;
    }

}

