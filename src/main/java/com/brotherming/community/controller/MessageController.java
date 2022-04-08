package com.brotherming.community.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brotherming.community.entity.Message;
import com.brotherming.community.entity.PageInfo;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.MessageService;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Controller
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @GetMapping("/letter/list")
    public String getLetterList(Model model, PageInfo pageInfo){
        User user = hostHolder.getUser();
        //分页信息
        pageInfo.setLimit(5);
        pageInfo.setPath("/message/letter/list");
        pageInfo.setRows(messageService.selectConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.selectConversations(user.getId(), pageInfo);
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (CollUtil.isNotEmpty(conversationList)) {
            for (Message message : conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                //会话总条数
                Integer count = messageService.lambdaQuery().ne(Message::getStatus, 2).ne(Message::getFromId, 1)
                        .eq(Message::getConversationId, message.getConversationId()).count();
                map.put("letterCount",count);
                //某一个会话的未读数量，当前用户作为接收者
                count = messageService.lambdaQuery().eq(Message::getStatus, 0).ne(Message::getFromId, 1)
                        .eq(Message::getConversationId, message.getConversationId()).eq(Message::getToId,user.getId()).count();
                map.put("unreadCount",count);
                //如果当前用户是发送方，就返回接收方的用户信息，否则反之
                int targetId = Objects.equals(user.getId(), message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target",userService.getById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //总的未读数量，查询未读消息数量
        int letterUnreadCount = messageService.lambdaQuery().eq(Message::getStatus, 0).ne(Message::getFromId, 1)
                .eq(Message::getToId,user.getId()).count();
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, PageInfo pageInfo, Model model){
        //分页信息
        pageInfo.setLimit(5);
        pageInfo.setPath("/message/letter/detail/" + conversationId);
        Integer count = messageService.lambdaQuery().ne(Message::getStatus, 2).ne(Message::getFromId, 1)
                .eq(Message::getConversationId, conversationId).count();
        pageInfo.setRows(count);

        //私信列表
        Page<Message> page = new Page<>(pageInfo.getCurrent(),pageInfo.getLimit());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        //根据 conversationId 查询所有会话
        wrapper.ne(Message::getStatus,2).ne(Message::getFromId,1)
                .eq(Message::getConversationId,conversationId)
                .orderByDesc(Message::getId);
        List<Message> letterList = messageService.page(page, wrapper).getRecords();
        List<Map<String,Object>> letters = new ArrayList<>();
        if (CollUtil.isNotEmpty(letterList)) {
            for (Message message : letterList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.getById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.getById(id1);
        }else {
            return userService.getById(id0);
        }
    }

}

