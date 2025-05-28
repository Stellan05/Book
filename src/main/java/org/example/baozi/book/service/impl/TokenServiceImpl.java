package org.example.baozi.book.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.service.TokenService;
import org.example.baozi.book.util.JWTUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token服务实现类
 * 负责管理JWT token，实现Token的刷新、验证和黑名单功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    
    private final RedisServiceImpl redisService;
    
    // Token相关的Redis键前缀
    private static final String USER_TOKEN_KEY = RedisServiceImpl.KEY_PREFIX_TOKEN + "user:";
    private static final String BLACKLIST_KEY = RedisServiceImpl.KEY_PREFIX_BLACKLIST;
    private static final long DEFAULT_TOKEN_EXPIRE = 24 * 60 * 60; // 默认24小时

    /**
     * 刷新Token
     * @param oldToken 旧Token
     * @return 新Token，如果旧Token无效则返回null
     */
    @Override
    public String refreshToken(String oldToken) {
        // 检查Token是否在黑名单中
        if (isInBlacklist(oldToken)) {
            log.warn("尝试刷新黑名单中的Token: {}", oldToken);
            return null;
        }
        
        // 使用JWTUtil刷新Token
        String newToken = JWTUtil.refreshToken(oldToken);
        if (newToken == null) {
            log.warn("Token刷新失败，可能是无效的Token: {}", oldToken);
            return null;
        }
        
        // 获取用户ID
        Integer userId = JWTUtil.getIdFromToken(oldToken);
        if (userId == null || userId <= 0) {
            log.warn("无法从Token中获取有效的用户ID: {}", oldToken);
            return null;
        }
        
        // 将旧Token加入黑名单，防止它被再次使用
        addToBlacklist(oldToken, "已刷新", DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
        
        // 保存新Token与用户的关联
        saveTokenForUser(newToken, userId, DEFAULT_TOKEN_EXPIRE, TimeUnit.SECONDS);
        
        return newToken;
    }

    /**
     * 将Token加入黑名单
     * @param token 要加入黑名单的Token
     * @param reason 加入黑名单的原因
     * @param expireTime 黑名单保留时间
     * @param timeUnit 时间单位
     * @return 是否成功加入黑名单
     */
    @Override
    public boolean addToBlacklist(String token, String reason, long expireTime, TimeUnit timeUnit) {
        try {
            // 以Token为键，原因为值，存入黑名单
            String blacklistKey = BLACKLIST_KEY + token;
            redisService.setValueWithExpire(blacklistKey, reason, expireTime, timeUnit);
            log.info("Token已加入黑名单: {}, 原因: {}", token, reason);
            return true;
        } catch (Exception e) {
            log.error("将Token加入黑名单时发生错误: {}", token, e);
            return false;
        }
    }

    /**
     * 检查Token是否在黑名单中
     * @param token 要检查的Token
     * @return 是否在黑名单中
     */
    @Override
    public boolean isInBlacklist(String token) {
        String blacklistKey = BLACKLIST_KEY + token;
        return redisService.hasKey(blacklistKey);
    }

    /**
     * 移除黑名单中的Token
     * @param token 要移除的Token
     * @return 是否成功移除
     */
    @Override
    public boolean removeFromBlacklist(String token) {
        String blacklistKey = BLACKLIST_KEY + token;
        return redisService.deleteValue(blacklistKey);
    }

    /**
     * 根据用户ID禁用所有Token
     * @param userId 用户ID
     * @param reason 禁用原因
     * @param expireTime 禁用时间
     * @param timeUnit 时间单位
     * @return 禁用的Token数量
     */
    @Override
    public int disableUserTokens(Integer userId, String reason, long expireTime, TimeUnit timeUnit) {
        String userTokenKey = USER_TOKEN_KEY + userId;
        
        // 获取用户所有的Token
        var tokens = redisService.getSetMembers(userTokenKey);
        if (tokens == null || tokens.isEmpty()) {
            log.info("用户没有活跃的Token: {}", userId);
            return 0;
        }
        
        int count = 0;
        for (Object tokenObj : tokens) {
            String token = (String) tokenObj;
            if (addToBlacklist(token, reason, expireTime, timeUnit)) {
                count++;
            }
        }
        
        log.info("已禁用用户 {} 的 {} 个Token", userId, count);
        return count;
    }

    /**
     * 保存Token和用户ID的关联，用于后续查询用户的所有Token
     * @param token Token
     * @param userId 用户ID
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 是否保存成功
     */
    @Override
    public boolean saveTokenForUser(String token, Integer userId, long expireTime, TimeUnit timeUnit) {
        try {
            String userTokenKey = USER_TOKEN_KEY + userId;
            
            // 将Token添加到用户的Token集合中
            redisService.addToSet(userTokenKey, token);
            
            // 设置集合的过期时间
            redisService.refreshExpire(userTokenKey, expireTime, timeUnit);
            
            log.debug("Token已保存至用户 {}: {}", userId, token);
            return true;
        } catch (Exception e) {
            log.error("保存用户Token时发生错误: userId={}, token={}", userId, token, e);
            return false;
        }
    }

    /**
     * 验证Token是否有效
     * @param token 要验证的Token
     * @return 是否有效
     */
    @Override
    public boolean validateToken(String token) {
        // 首先检查Token是否在黑名单中
        if (isInBlacklist(token)) {
            log.warn("Token在黑名单中: {}", token);
            return false;
        }
        
        // 然后使用JWTUtil验证Token的有效性
        return JWTUtil.validateToken(token);
    }

    /**
     * 获取Token中包含的用户ID
     * @param token Token
     * @return 用户ID，如果Token无效则返回null
     */
    @Override
    public Integer getUserIdFromToken(String token) {
        // 检查Token是否有效
        if (!validateToken(token)) {
            log.warn("从无效的Token中获取用户ID: {}", token);
            return null;
        }
        
        return JWTUtil.getIdFromToken(token);
    }

    /**
     * 获取Token中包含的用户名
     * @param token Token
     * @return 用户名，如果Token无效则返回null
     */
    @Override
    public String getUsernameFromToken(String token) {
        // 检查Token是否有效
        if (!validateToken(token)) {
            log.warn("从无效的Token中获取用户名: {}", token);
            return null;
        }
        
        return JWTUtil.getUsernameFromToken(token);
    }

    /**
     * 获取Token中包含的用户角色
     * @param token Token
     * @return 用户角色，如果Token无效则返回null
     */
    @Override
    public String getRolesFromToken(String token) {
        // 检查Token是否有效
        if (!validateToken(token)) {
            log.warn("从无效的Token中获取用户角色: {}", token);
            return null;
        }
        
        return JWTUtil.getRoleFromToken(token);
    }
} 