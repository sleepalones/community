package com.brotherming.community.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String,Object> map = userService.register(user);
        if (CollUtil.isEmpty(map)) {
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId,code);
        if (result == CommunityConstant.ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用!");
            model.addAttribute("target","/login");
        } else if (result == CommunityConstant.ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，重复激活!");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletResponse response/*, HttpSession session*/) {
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(100, 40,4,4);
        //session.setAttribute("captcha",circleCaptcha.getCode());

        //验证码的归属
        String captchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("captchaOwner",captchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String captchaKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
        redisTemplate.opsForValue().set(captchaKey,circleCaptcha.getCode(),60, TimeUnit.SECONDS);

        response.setContentType("image/png");
        try {
            circleCaptcha.write(response.getOutputStream());
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(Model model, /*HttpSession session,*/ HttpServletResponse response,
                        String username, String password, String code, boolean rememberme,
                        @CookieValue("captchaOwner") String captchaOwner) {
        // 检查验证码
        //String captcha = (String) session.getAttribute("captcha");

        String captcha = null;
        if (StrUtil.isNotBlank(captchaOwner)) {
            String captchaKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
            captcha = (String) redisTemplate.opsForValue().get(captchaKey);
        }

        if (StrUtil.isBlank(captcha) || StrUtil.isBlank(code) || !captcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码不正确!");
            return "/site/login";
        }

        //检查账号密码
        int expiredSeconds = rememberme ? CommunityConstant.REMEMBER_EXPIRED_SECONDS : CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}
