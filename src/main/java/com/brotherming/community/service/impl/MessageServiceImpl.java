package com.brotherming.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.MessageMapper;
import com.brotherming.community.entity.Message;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Override
    public List<Message> selectConversations(int userId, PageInfo pageInfo) {
        return messageMapper.selectConversations(userId,pageInfo.getCurrent(),pageInfo.getLimit());
    }

    @Override
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }
}
