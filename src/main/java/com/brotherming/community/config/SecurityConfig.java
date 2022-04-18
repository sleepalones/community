package com.brotherming.community.config;

import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.io.PrintWriter;

/**
 * @author brotherming
 * @createTime 2022年04月18日 15:16:00
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discussPost/add",
                        "/comment/add/**",
                        "/message/**/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                ).hasAnyAuthority(
                    AUTHORITY_USER,
                    AUTHORITY_ADMIN,
                    AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discussPost/top",
                        "/discussPost/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discussPost/delete"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                ).anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    //没有登录
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦!"));
                    }else {
                        response.sendRedirect(request.getContextPath() + "/login");
                    }
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    //权限不足
                    String xRequestedWith = request.getHeader("x-requested-with");
                    if ("XMLHttpRequest".equals(xRequestedWith)) {
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限!"));
                    }else {
                        response.sendRedirect(request.getContextPath() + "/denied");
                    }
                });

        //security 底层默认会拦截 /logout 请求，进行退出处理
        //覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/securitylogout");
    }
}
