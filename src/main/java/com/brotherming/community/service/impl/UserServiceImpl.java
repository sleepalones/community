package com.brotherming.community.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.LoginTicketMapper;
import com.brotherming.community.dao.UserMapper;
import com.brotherming.community.entity.LoginTicket;
import com.brotherming.community.entity.User;
import com.brotherming.community.service.UserService;
import com.brotherming.community.util.CommunityConstant;
import com.brotherming.community.util.CommunityUtil;
import com.brotherming.community.util.MailClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private LoginTicketMapper loginTicketMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public Map<String, Object> register(User user) {
        Map<String,Object> map = new HashMap<>();
        //处理空值
        if (ObjectUtil.isEmpty(user)) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StrUtil.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StrUtil.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if (StrUtil.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        User one = new LambdaQueryChainWrapper<>(userMapper).eq(User::getUsername, user.getUsername()).one();
        if (ObjectUtil.isNotEmpty(one)) {
            map.put("usernameMsg","该账号已存在!");
            return map;
        }
        one = new LambdaQueryChainWrapper<>(userMapper).eq(User::getEmail,user.getEmail()).one();
        if (ObjectUtil.isNotEmpty(one)) {
            map.put("emailMsg","该邮箱已被注册!");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //noinspection HttpUrlsUsage
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(DateUtil.toLocalDateTime(new Date()));
        userMapper.insert(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    @Override
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return CommunityConstant.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            user.setStatus(1);
            userMapper.updateById(user);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else {
            return CommunityConstant.ACTIVATION_FAIL;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //处理空值
        if (StrUtil.isBlank(username)) {
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StrUtil.isBlank(password)) {
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        //验证账号
        User user = new LambdaQueryChainWrapper<>(userMapper).eq(User::getUsername,username).one();
        if (ObjectUtil.isEmpty(user)) {
            map.put("usernameMsg","该账号不存在!");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg","该账号未激活!");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码不正确!");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(DateUtil.toLocalDateTime(new Date(System.currentTimeMillis() + expiredSeconds * 1000L)));
        loginTicketMapper.insert(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
        new LambdaUpdateChainWrapper<>(loginTicketMapper)
                .eq(LoginTicket::getTicket,ticket).set(LoginTicket::getStatus,1).update();
    }

    @Override
    public Map<String, Object> updatePassword(String oldpassword, String newpassword, String confirmpassword, User user) {
        Map<String,Object> map = new HashMap<>();
        //处理空值
        if (StrUtil.isBlank(oldpassword)) {
            map.put("old","原密码不能为空!");
            return map;
        }
        if (StrUtil.isBlank(newpassword)) {
            map.put("new","新密码不能为空!");
            return map;
        }
        if (StrUtil.isBlank(oldpassword)) {
            map.put("confirm","确认密码不能为空!");
            return map;
        }
        //比较新密码和确认密码
        if (!newpassword.equals(confirmpassword)) {
            map.put("confirm","两次输入结果不同!");
            return map;
        }
        String oldMd5 = CommunityUtil.md5(oldpassword + user.getSalt());
        if (!user.getPassword().equals(oldMd5)) {
            map.put("old","原密码不正确");
            return map;
        }
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        String newMd5 = CommunityUtil.md5(newpassword + user.getSalt());
        user.setPassword(newMd5);
        userMapper.updateById(user);
        return map;
    }
}
