package com.brotherming.community.controller.interceptor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.brotherming.community.entity.LoginTicket;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.LoginTicketService;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CookieUtil;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Resource
    private LoginTicketService loginTicketService;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (StrUtil.isNotBlank(ticket)) {
            //查询凭证
            LoginTicket loginTicket = loginTicketService.lambdaQuery().eq(LoginTicket::getTicket, ticket).one();
            //检查凭证是否有效
            if (ObjectUtil.isNotEmpty(loginTicket) && loginTicket.getStatus() == 0 &&
                    loginTicket.getExpired().isAfter(DateUtil.toLocalDateTime(new Date()))){
                User user = userService.lambdaQuery().eq(User::getId, loginTicket.getUserId()).one();
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (ObjectUtil.isNotEmpty(user) && ObjectUtil.isNotEmpty(modelAndView)) {
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
