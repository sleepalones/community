package com.brotherming.community.controller;

import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.service.ElasticsearchService;
import com.brotherming.community.service.LikeService;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author brotherming
 * @createTime 2022年04月17日 12:42:00
 */

@Controller
public class SearchController {

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, PageInfo pageInfo, Model model) {
        //搜索帖子
        pageInfo.setLimit(10);
        Page<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, pageInfo.getCurrent() - 1, pageInfo.getLimit());
        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        //分页信息
        pageInfo.setPath("/search?keyword=" + keyword);
        pageInfo.setRows(0);
        Optional.ofNullable(searchResult).ifPresent(result -> {
            result.forEach(post -> {
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.getById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            });
            //不为空则设置 Rows
            pageInfo.setRows((int) result.getTotalElements());
        });
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        return "/site/search";
    }

}
