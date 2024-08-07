package org.bzk.documentserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bzk.documentserver.bean.User;
import org.bzk.documentserver.mapper.UserMapper;
import org.bzk.documentserver.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
