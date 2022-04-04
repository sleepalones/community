package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.User;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
public interface UserService extends IService<User> {

    Map<String, Object> register(User user);

    int activation(int userId, String code);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);

}
