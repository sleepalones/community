package com.brotherming.community.service.impl;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateUtil;
import com.brotherming.community.service.StatisticsService;
import com.brotherming.community.util.RedisKeyUtil;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author brotherming
 * @createTime 2022年04月19日 09:53:00
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(DateUtil.format(new Date(), "yyyyMMdd"));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }

    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = CalendarUtil.calendar();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(DateUtil.format(calendar.getTime(),"yyyyMMdd"));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }

        //合并这些数据
        String uvKey = RedisKeyUtil.getUVKey(DateUtil.format(start, "yyyyMMdd"), DateUtil.format(end, "yyyyMMdd"));
        redisTemplate.opsForHyperLogLog().union(uvKey,keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }

    @Override
    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(DateUtil.format(new Date(), "yyyyMMdd"));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    @Override
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = CalendarUtil.calendar();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(DateUtil.format(calendar.getTime(),"yyyyMMdd"));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        return (long) redisTemplate.execute((RedisCallback) connection -> {
            String dauKey = RedisKeyUtil.getDAUKey(DateUtil.format(start, "yyyyMMdd"), DateUtil.format(end, "yyyyMMdd"));
            connection.bitOp(RedisStringCommands.BitOperation.OR,dauKey.getBytes(),keyList.toArray(new byte[0][0]));
            return connection.bitCount(dauKey.getBytes());
        });
    }
}
