package org.example.baozi.book.service;

/**
 * 认证服务接口
 * 处理用户认证和JWT令牌生成
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌
     */
    String login(String username, String password);
    
    /**
     * 刷新用户Token
     * @param oldToken 旧Token
     * @return 新的Token，如果旧Token无效则返回null
     */
    String refreshToken(String oldToken);
    
    /**
     * 用户退出登录
     * @param token 用户的Token
     * @return 是否成功退出
     */
    boolean logout(String token);
    
    /**
     * 检查用户状态
     * @param username 用户名
     * @return 用户是否被禁用
     */
    boolean isUserDisabled(String username);
}
