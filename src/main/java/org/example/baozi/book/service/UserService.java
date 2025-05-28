package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);
} 