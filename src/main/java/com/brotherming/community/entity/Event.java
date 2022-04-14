package com.brotherming.community.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件
 */
@Data
public class Event {

    //主题
    private String topic;
    //事件的来源，触发事件的人
    private int userId;
    //事件发生的类型
    private int entityType;
    //事件发生的实体
    private int entityId;
    //实体的作者
    private int entityUserId;
    //扩展信息
    private Map<String,Object> data = new HashMap<>();

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key,value);
        return this;
    }
}
