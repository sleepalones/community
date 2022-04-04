package com.brotherming.community.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.brotherming.community.dao.UserMapper;
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
}
