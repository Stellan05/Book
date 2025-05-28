package org.example.baozi.book.security;

import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义UserDetailsService实现
 * 负责从数据库加载用户信息并转换为Spring Security需要的UserDetails对象
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    
    /**
     * 根据用户名加载用户
     * @param username 用户名
     * @return UserDetails对象
     * @throws UsernameNotFoundException 如果用户不存在则抛出异常
     */
    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中查找用户
        User user = userService.getUserByUsername(username);
        
        // 如果用户不存在，抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return new UserDetailsImpl(user);
    }
} 