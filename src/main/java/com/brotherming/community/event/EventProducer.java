package com.brotherming.community.event;

import cn.hutool.json.JSONUtil;
import com.brotherming.community.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 事件生产者
 */
@Component
public class EventProducer {

    @Resource
    private KafkaTemplate<String,Object> kafkaTemplate;

    //处理事件
    public void fireEvent(Event event) {
        //将事件发布到指定主题
        kafkaTemplate.send(event.getTopic(), JSONUtil.toJsonStr(event));
    }

}
