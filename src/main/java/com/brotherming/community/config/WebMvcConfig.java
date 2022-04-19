package com.brotherming.community.config;

import com.brotherming.community.controller.interceptor.LoginTicketInterceptor;
import com.brotherming.community.controller.interceptor.MessageInterceptor;
import com.brotherming.community.controller.interceptor.StatisticsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginTicketInterceptor loginTicketInterceptor;

    /*@Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;*/

    @Resource
    private MessageInterceptor messageInterceptor;

    @Resource
    private StatisticsInterceptor statisticsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        //registry.addInterceptor(loginRequiredInterceptor)
                //.excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(statisticsInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
