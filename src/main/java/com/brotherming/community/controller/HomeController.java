package com.brotherming.community.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @GetMapping("/index")
    public String getIndexPage(Model model, PageInfo pageInfo){
        Page<DiscussPost> page = new Page<>(pageInfo.getCurrent(),pageInfo.getLimit());
        LambdaQueryWrapper<DiscussPost> wrapper = new LambdaQueryWrapper<>();
        // 查询没有被拉黑的帖子
        wrapper.ne(DiscussPost::getStatus,2);
        wrapper.orderByDesc(DiscussPost::getType).orderByDesc(DiscussPost::getCreateTime);
        Page<DiscussPost> discussPostPage = discussPostService.page(page,wrapper);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (CollUtil.isNotEmpty(discussPostPage.getRecords())) {
            discussPostPage.getRecords().forEach(dis -> {
                Map<String,Object> map = new HashMap<>();
                map.put("post",dis);
                User user = userService.findUserById(Integer.parseInt(dis.getUserId()));
                map.put("user",user);
                map.put("likeCount", likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,dis.getId()));
                discussPosts.add(map);
            });
        }
        model.addAttribute("discussPosts",discussPosts);
        pageInfo.setPath("/index");
        pageInfo.setRows((int) page.getTotal());
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
