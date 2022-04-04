package com.brotherming.community.controller;

import cn.hutool.core.collection.CollUtil;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.Map;

@Controller
public class LoginController {

    @Resource
    private UserService userService;

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
}
