package com.brotherming.community.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.brotherming.community.entity.Event;
import com.brotherming.community.entity.Message;
import com.brotherming.community.service.MessageService;
import com.brotherming.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件消费者
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Resource
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord<String,Object> record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }
        Event event = JSONUtil.toBean(record.value().toString(), Event.class);
        if (ObjectUtil.isEmpty(event)) {
            logger.error("消息格式错误!");
            return;
        }
        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setStatus(0);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(DateUtil.toLocalDateTime(new Date()));

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (CollUtil.isNotEmpty(event.getData())) {
            content.putAll(event.getData());
        }

        message.setContent(JSONUtil.toJsonStr(content));

        messageService.addMessage(message);
    }
}
