package com.brotherming.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.brotherming.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
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

    Map<String, Object> updatePassword(String oldpassword, String newpassword, String confirmpassword, User user);

    //优先从缓存中取值
    User getCache(int userId);

    //取不到时初始化缓存数据
    User initCache(int userId);

    //数据变更时清除缓存数据
    void clearCache(int userId);

    User findUserById(int userId);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);

}
