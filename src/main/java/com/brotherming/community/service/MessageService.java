package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.Message;
import com.brotherming.community.entity.PageInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
public interface MessageService extends IService<Message> {

    List<Message> selectConversations(int userId, PageInfo pageInfo);

    int selectConversationCount(int userId);

    int addMessage(Message message);

    Message findLatestNotice(Integer userId, String topic);
}
