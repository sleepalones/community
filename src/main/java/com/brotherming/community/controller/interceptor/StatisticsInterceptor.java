package com.brotherming.community.controller.interceptor;

import com.brotherming.community.service.StatisticsService;
import com.brotherming.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author brotherming
 * @createTime 2022年04月19日 10:45:00
 */
@Component
public class StatisticsInterceptor implements HandlerInterceptor {

    @Resource
    private StatisticsService statisticsService;

    @Resource
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        statisticsService.recordUV(request.getRemoteHost());
        //统计DAU
        Optional.ofNullable(hostHolder.getUser()).ifPresent(user -> statisticsService.recordDAU(user.getId()));
        return true;
    }
}
