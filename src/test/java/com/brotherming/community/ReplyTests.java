package com.brotherming.community;

import com.brotherming.community.entity.Comment;
import com.brotherming.community.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author brotherming
 * @createTime 2022年04月21日 14:44:00
 */
@SpringBootTest
public class ReplyTests {

    @Resource
    private CommentService commentService;

    @Test
    void test() {
        List<Comment> comments = commentService.lambdaQuery().eq(Comment::getUserId, 111).orderByDesc(Comment::getCreateTime).list();
        Map<Integer, List<Comment>> collect = comments.stream().sorted(Comparator.comparing(Comment::getCreateTime).reversed()).collect(Collectors.groupingBy(Comment::getEntityId));
        System.out.println(collect);
    }

}
