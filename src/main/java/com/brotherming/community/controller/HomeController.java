package com.brotherming.community.controller;

import cn.hutool.core.collection.CollUtil;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.service.LikeService;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区首页
 */
@Controller
public class HomeController {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @GetMapping("/")
    public String root() {
        return "forward:/index";
    }

    @GetMapping("/index")
    public String getIndexPage(Model model, PageInfo pageInfo,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        List<DiscussPost> discussPostPage = discussPostService.findDiscussPostPage(pageInfo,orderMode);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (CollUtil.isNotEmpty(discussPostPage)) {
            discussPostPage.forEach(dis -> {
                Map<String,Object> map = new HashMap<>();
                map.put("post",dis);
                User user = userService.findUserById(Integer.parseInt(dis.getUserId()));
                map.put("user",user);
                map.put("likeCount", likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,dis.getId()));
                discussPosts.add(map);
            });
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        pageInfo.setPath("/index?orderMode=" + orderMode);
        int total = discussPostService.findDiscussPostTotal(orderMode);
        pageInfo.setRows(total);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }

}
