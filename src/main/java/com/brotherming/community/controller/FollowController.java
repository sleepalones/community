package com.brotherming.community.controller;

import com.brotherming.community.entity.User;
import com.brotherming.community.service.FollowService;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.HostHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class FollowController {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private FollowService followService;

    @PostMapping("/follow")
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @PostMapping("/unfollow")
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

}
