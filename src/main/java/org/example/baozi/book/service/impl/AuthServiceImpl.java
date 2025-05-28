package org.example.baozi.book.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.security.UserDetailsImpl;
import org.example.baozi.book.security.UserDetailsServiceImpl;
import org.example.baozi.book.service.AuthService;
import org.example.baozi.book.service.StudentService;
import org.example.baozi.book.service.TokenService;
import org.example.baozi.book.util.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 处理用户认证、Token生成、刷新和用户状态管理
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;
    private final RedisServiceImpl redisService;
    private final StudentService studentService;
    
    // Redis键前缀
    private static final String USER_STATUS_KEY = RedisServiceImpl.KEY_PREFIX_USER + "status:";
    private static final long DEFAULT_TOKEN_EXPIRE = 24 * 60 * 60; // 24小时

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌
     */
    @Override
    public String login(String username, String password) {
        // 检查用户是否被禁用
        if (isUserDisabled(username)) {
            throw new RuntimeException("用户已被禁用，无法登录");
        }
        
        // 认证逻辑
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if(!authentication.isAuthenticated()){
            throw new RuntimeException("登录失败");
        }

        // 加载用户详情
        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);

        // 生成Token
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("username", username);
        claims.put("roles", userDetails.getAuthorities().toString());

        // 生成JWT令牌
        String token = JWTUtil.generateToken(claims);
        
        // 保存Token与用户的关联，便于后续管理（如禁用所有Token）
        tokenService.saveTokenForUser(token, userDetails.getUserId(), DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
        
        // 将用户信息缓存到Redis，提高后续访问性能
        String userKey = RedisServiceImpl.KEY_PREFIX_USER + username;
        redisService.setValueWithExpire(userKey, claims, DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
        
        return token;
    }

    /**
     * 刷新用户Token
     * @param oldToken 旧Token
     * @return 新的Token，如果旧Token无效则返回null
     */
    @Override
    public String refreshToken(String oldToken) {
        return tokenService.refreshToken(oldToken);
    }

    /**
     * 用户退出登录
     * @param token 用户的Token
     * @return 是否成功退出
     */
    @Override
    public boolean logout(String token) {
        // 获取用户信息
        String username = tokenService.getUsernameFromToken(token);
        if (username == null) {
            return false;
        }
        
        // 将Token加入黑名单
        return tokenService.addToBlacklist(token, "用户主动退出", DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 检查用户状态
     * @param username 用户名
     * @return 用户是否被禁用
     */
    @Override
    public boolean isUserDisabled(String username) {
        // 首先检查Redis缓存中是否有用户状态
        String statusKey = USER_STATUS_KEY + username;
        Object status = redisService.getValue(statusKey);
        
        if (status != null) {
            // 如果缓存中有状态信息，直接返回
            return Boolean.TRUE.equals(status);
        }
        
        // 如果缓存中没有，查询数据库
        if (username.length() == 12) { // 学生ID长度为12位
            Student student = studentService.getStudentById(username);
            if (student != null) {
                boolean disabled = !student.getStatus(); // student.status为true表示正常，false表示禁用
                
                // 将状态信息缓存到Redis，提高后续访问性能
                redisService.setValueWithExpire(statusKey, disabled, DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
                
                return disabled;
            }
        }
        
        // 默认情况下，假设用户未被禁用
        return false;
    }
}
