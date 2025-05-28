package org.example.baozi.book.service;

import java.util.concurrent.TimeUnit;

/**
 * Token服务接口
 * 负责管理JWT token，包括Token存储、刷新、验证和黑名单功能
 */
public interface TokenService {
    
    /**
     * 刷新Token
     * @param oldToken 旧Token
     * @return 新Token，如果旧Token无效则返回null
     */
    String refreshToken(String oldToken);
    
    /**
     * 将Token加入黑名单
     * @param token 要加入黑名单的Token
     * @param reason 加入黑名单的原因
     * @param expireTime 黑名单保留时间
     * @param timeUnit 时间单位
     * @return 是否成功加入黑名单
     */
    boolean addToBlacklist(String token, String reason, long expireTime, TimeUnit timeUnit);
    
    /**
     * 检查Token是否在黑名单中
     * @param token 要检查的Token
     * @return 是否在黑名单中
     */
    boolean isInBlacklist(String token);
    
    /**
     * 移除黑名单中的Token
     * @param token 要移除的Token
     * @return 是否成功移除
     */
    boolean removeFromBlacklist(String token);
    
    /**
     * 根据用户ID禁用所有Token
     * @param userId 用户ID
     * @param reason 禁用原因
     * @param expireTime 禁用时间
     * @param timeUnit 时间单位
     * @return 禁用的Token数量
     */
    int disableUserTokens(Integer userId, String reason, long expireTime, TimeUnit timeUnit);
    
    /**
     * An保存Token和用户ID的关联，用于后续查询用户的所有Token
     * @param token Token
     * @param userId 用户ID
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 是否保存成功
     */
    boolean saveTokenForUser(String token, Integer userId, long expireTime, TimeUnit timeUnit);
    
    /**
     * 验证Token是否有效
     * @param token 要验证的Token
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 获取Token中包含的用户ID
     * @param token Token
     * @return 用户ID，如果Token无效则返回null
     */
    Integer getUserIdFromToken(String token);
    
    /**
     * 获取Token中包含的用户名
     * @param token Token
     * @return 用户名，如果Token无效则返回null
     */
    String getUsernameFromToken(String token);
    
    /**
     * 获取Token中包含的用户角色
     * @param token Token
     * @return 用户角色，如果Token无效则返回null
     */
    String getRolesFromToken(String token);
} 