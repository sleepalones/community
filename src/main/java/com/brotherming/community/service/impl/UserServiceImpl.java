package com.brotherming.community.service.impl;

import com.brotherming.community.entity.User;
import com.brotherming.community.dao.UserMapper;
import com.brotherming.community.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
