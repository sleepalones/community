package com.brotherming.community.service.impl;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.MessageMapper;
import com.brotherming.community.entity.Message;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.service.MessageService;
import com.brotherming.community.util.SensitiveFilter;
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

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> selectConversations(int userId, PageInfo pageInfo) {
        return messageMapper.selectConversations(userId,pageInfo.getOffset(),pageInfo.getLimit());
    }

    @Override
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public int addMessage(Message message) {
        message.setContent(HtmlUtil.escape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insert(message);
    }

    @Override
    public Message findLatestNotice(Integer id, String topic) {
        return messageMapper.selectLatestNotice(id,topic);
    }
}
