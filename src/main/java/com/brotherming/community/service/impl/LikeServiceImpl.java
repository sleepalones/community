package com.brotherming.community.service.impl;

import com.brotherming.community.service.LikeService;
import com.brotherming.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikeServiceImpl implements LikeService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 点赞
     * @param userId 点赞人
     * @param entityType 点赞的类型
     * @param entityId 点赞的目标
     */
    @Override
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        //查询redis中有没有该数据，有则删除，没有则添加（点赞与取消点赞）
        if (isMember != null && isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else {
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }
    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    @Override
    public int findUserLikeCount(int userId) {
        return 0;
    }
}
