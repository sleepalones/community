package com.brotherming.community.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brotherming.community.entity.*;
import com.brotherming.community.event.EventProducer;
import com.brotherming.community.service.CommentService;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.service.LikeService;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @PostMapping("/add")
    @ResponseBody
    public String add(String title, String content) {
        User user = hostHolder.getUser();
        if (ObjectUtil.isEmpty(user)) {
            return CommunityUtil.getJSONString(403,"未登录不能操作");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(String.valueOf(user.getId()));
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(DateUtil.toLocalDateTime(new Date()));
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(post.getId());

        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int discussPostId, PageInfo pageInfo){
        //帖子
        DiscussPost post = discussPostService.getById(discussPostId);
        model.addAttribute("post",post);
        //作者
        User user = userService.findUserById(Integer.parseInt(post.getUserId()));
        model.addAttribute("user",user);
        //点赞数量-帖子
        long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        pageInfo.setPath("/discussPost/detail/" + discussPostId);
        pageInfo.setRows(post.getCommentCount());

        //分页查询评论列表，帖子地下的评论
        Page<Comment> page = new Page<>(pageInfo.getCurrent(),pageInfo.getLimit());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getStatus,0);
        wrapper.eq(Comment::getEntityType, CommunityConstant.ENTITY_TYPE_POST)
                .eq(Comment::getEntityId,post.getId())
                .orderByAsc(Comment::getCreateTime);
        //评论列表
        Page<Comment> commentPage = commentService.page(page,wrapper);
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        //查询到评论列表后再将每条评论的作者查询出来封装到map集合中
        if (CollUtil.isNotEmpty(commentPage.getRecords())) {
            for (Comment comment : commentPage.getRecords()) {
                Map<String,Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //回复列表，查询在这条评论下有没有评论，和回复
                List<Comment> replyList = commentService.lambdaQuery()
                        .eq(Comment::getEntityType, CommunityConstant.ENTITY_TYPE_COMMENT)
                        .eq(Comment::getEntityId, comment.getId()).list();

                List<Map<String,Object>> replyVoList = new ArrayList<>();
                //如果还有回复就将回复的作者信息和该回复封装到map中
                if (CollUtil.isNotEmpty(replyList)) {
                    for (Comment reply : replyList) {
                        Map<String,Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                //回复数量
                Integer replyCount = commentService.lambdaQuery().eq(Comment::getEntityType, CommunityConstant.ENTITY_TYPE_COMMENT)
                        .eq(Comment::getEntityId, comment.getId()).count();
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.lambdaUpdate().set(DiscussPost::getType, 1).eq(DiscussPost::getId, id).update();
        //触发发帖事件
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,null);
    }

    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.lambdaUpdate().set(DiscussPost::getStatus, 1).eq(DiscussPost::getId, id).update();
        //触发发帖事件
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,null);
    }

    //加精
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.lambdaUpdate().set(DiscussPost::getStatus, 2).eq(DiscussPost::getId, id).update();
        //触发发帖事件
        Event event = new Event()
                .setTopic(CommunityConstant.TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommunityConstant.ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,null);
    }
}

