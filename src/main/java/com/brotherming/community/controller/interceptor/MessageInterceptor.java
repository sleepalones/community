package com.brotherming.community.controller.interceptor;

import com.brotherming.community.entity.Message;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.MessageService;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int letterUnreadCount = messageService.lambdaQuery().eq(Message::getStatus, 0).ne(Message::getFromId, 1)
                    .eq(Message::getToId,user.getId()).count();
            int noticeUnreadCount = messageService.lambdaQuery().eq(Message::getStatus, 0).eq(Message::getFromId, 1)
                    .eq(Message::getToId, user.getId()).count();
            modelAndView.addObject("allUnreadCount",letterUnreadCount + noticeUnreadCount);
        }
    }
}
