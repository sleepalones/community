package com.brotherming.community;

import cn.hutool.core.date.DateUtil;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author brotherming
 * @createTime 2022年04月20日 22:45:00
 */
@SpringBootTest
public class CaffeineTests {

    @Resource
    private DiscussPostService postService;

    //@Test
    public void init() {
        for (int i = 0; i < 500000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId("111");
            post.setTitle("2022秋招必胜：" + i);
            post.setContent("互联网行业越来越卷，球球给个offer吧，已经准备一年了，加油加油：" + i);
            post.setCreateTime(DateUtil.toLocalDateTime(new Date()));
            post.setScore(Math.random() * 2000);
            postService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrent(1);
        pageInfo.setLimit(10);
        System.out.println(postService.findDiscussPostPage(pageInfo,1));
        System.out.println(postService.findDiscussPostPage(pageInfo,1));
        System.out.println(postService.findDiscussPostPage(pageInfo,0));
    }

}
