package com.brotherming.community.quartz;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.brotherming.community.entity.DiscussPost;
import com.brotherming.community.service.DiscussPostService;
import com.brotherming.community.service.ElasticsearchService;
import com.brotherming.community.service.LikeService;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author brotherming
 * @createTime 2022年04月19日 16:19:00
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private LikeService likeService;

    @Resource
    private ElasticsearchService elasticsearchService;

    private static final Date EPOCH;

    static {
        EPOCH = DateUtil.parse("2021-04-01 00:00:00","yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations<String, Object> operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            logger.info("[任务取消] 暂时没有需要刷新的帖子!");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }

    private void refresh(Integer postId) {
        DiscussPost post = discussPostService.getById(postId);
        if (ObjectUtil.isEmpty(post)) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }
        //是否精华
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10L + likeCount * 2;
        //分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w,1)) + DateUtil.between(EPOCH,DateUtil.date(post.getCreateTime()), DateUnit.DAY);
        post.setScore(score);
        discussPostService.updateById(post);
        elasticsearchService.saveDiscussPost(post);
    }
}
