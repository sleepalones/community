package com.brotherming.community.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.HostHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/discussPost")
public class DiscussPostController {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private HostHolder hostHolder;

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
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

}

