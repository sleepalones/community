package com.brotherming.community.service.impl;

import com.brotherming.community.entity.Message;
import com.brotherming.community.dao.MessageMapper;
import com.brotherming.community.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
