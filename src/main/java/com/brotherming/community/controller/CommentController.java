package com.brotherming.community.controller;


import cn.hutool.core.date.DateUtil;
import com.brotherming.community.entity.Comment;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.Event;
import com.brotherming.community.event.EventProducer;
import com.brotherming.community.service.CommentService;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.util.CommunityConstant;
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

    @Resource
    private EventProducer eventProducer;

    @Resource
    private DiscussPostService discussPostService;

    @PostMapping("/add/{discussPostId}")
    private String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setTargetId(comment.getTargetId()==null?0:comment.getTargetId());
        comment.setCreateTime(DateUtil.toLocalDateTime(new Date()));
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);

        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.getById(comment.getEntityId());
            event.setEntityUserId(Integer.parseInt(target.getUserId()));
        } else if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_COMMENT) {
            Comment target = commentService.getById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(CommunityConstant.TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);

            eventProducer.fireEvent(event);
        }

        return "redirect:/discussPost/detail/" + discussPostId;
    }

}

