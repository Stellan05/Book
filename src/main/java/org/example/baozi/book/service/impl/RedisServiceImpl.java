package org.example.baozi.book.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 * 提供Redis操作的通用方法
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl {
    private final RedisTemplate<String, Object> redisTemplate;

    // 常量定义，用于统一管理Key前缀
    public static final String KEY_PREFIX_TOKEN = "token:";
    public static final String KEY_PREFIX_USER = "user:";
    public static final String KEY_PREFIX_STUDENT = "student:";
    public static final String KEY_PREFIX_BLACKLIST = "blacklist:";
    public static final String KEY_PREFIX_REPORT = "report:";
    public static final String KEY_PREFIX_BOOK = "book:";
    public static final String KEY_PREFIX_COLLECTOR = "collector:";

    /**
     * 存储键值对
     * @param key Redis的键
     * @param value 键对应的值
     */
    public void setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis set value success: key={}", key);
        } catch (Exception e) {
            log.error("Redis set value error: key={}", key, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    /**
     * 通过键获取值
     * @param key Redis键
     * @return 键对应的值
     */
    public Object getValue(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Redis get value: key={}, exists={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Redis get value error: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除键及其值
     * @param key Redis键
     * @return 是否删除成功
     */
    public boolean deleteValue(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Redis delete key: key={}, result={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis delete key error: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置键值对及其过期时间
     * @param key Redis键
     * @param value 键对应的值
     * @param expire 过期时间
     * @param timeUnit 时间单位
     */
    public void setValueWithExpire(String key, Object value, long expire, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, timeUnit);
            log.debug("Redis set value with expire: key={}, expire={}, timeUnit={}", key, expire, timeUnit);
        } catch (Exception e) {
            log.error("Redis set value with expire error: key={}, value={}", key, value, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    /**
     * 刷新键的过期时间
     * @param key Redis键
     * @param expire 过期时间
     * @param timeUnit 时间单位
     * @return 是否成功刷新
     */
    public boolean refreshExpire(String key, long expire, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.expire(key, expire, timeUnit);
            if (Boolean.TRUE.equals(result)) {
                log.debug("Redis refresh expire success: key={}, expire={}, timeUnit={}", key, expire, timeUnit);
                return true;
            } else {
                log.warn("Redis refresh expire failed: key={} not found", key);
                return false;
            }
        } catch (Exception e) {
            log.error("Redis refresh expire error: key={}", key, e);
            return false;
        }
    }

    /**
     * 判断键是否存在
     * @param key Redis键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis check key exists error: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取键的过期时间
     * @param key Redis键
     * @param timeUnit 返回的时间单位
     * @return 过期时间，-1表示永不过期，-2表示键不存在
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        try {
            return redisTemplate.getExpire(key, timeUnit);
        } catch (Exception e) {
            log.error("Redis get expire error: key={}", key, e);
            return -2;
        }
    }

    /**
     * 将元素添加到Set集合---Set集合具有自动去重，快速读取和删除单个token的优势
     * @param key Redis键
     * @param values 要添加的值
     * @return 添加成功的元素数量
     */
    public long addToSet(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis add to set error: key={}", key, e);
            return 0;
        }
    }

    /**
     * 判断元素是否在Set集合中
     * @param key Redis键
     * @param value 要判断的值
     * @return 是否存在
     */
    public boolean isInSet(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis check set member error: key={}, value={}", key, value, e);
            return false;
        }
    }

    /**
     * 获取Set集合的所有元素
     * @param key Redis键
     * @return 集合中的所有元素
     */
    public Set<Object> getSetMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis get set members error: key={}", key, e);
            return new HashSet<>();
        }
    }

    /**
     * 从Set中移除元素
     * @param key Redis键
     * @param values 要移除的值
     * @return 移除成功的元素数量
     */
    public long removeFromSet(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis remove from set error: key={}", key, e);
            return 0;
        }
    }

    /**
     * 将值添加到Hash
     * @param key Redis键
     * @param hashKey Hash的键
     * @param value 值
     */
    public void putHash(String key, Object hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Redis put hash error: key={}, hashKey={}", key, hashKey, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }

    /**
     * 获取Hash中的值
     * @param key Redis键
     * @param hashKey Hash的键
     * @return 值
     */
    public Object getHashValue(String key, Object hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("Redis get hash value error: key={}, hashKey={}", key, hashKey, e);
            return null;
        }
    }

    /**
     * 删除Hash中的键
     * @param key Redis键
     * @param hashKeys Hash的键
     * @return 删除成功的数量
     */
    public long deleteHashKeys(String key, Object... hashKeys) {
        try {
            Long count = redisTemplate.opsForHash().delete(key, hashKeys);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis delete hash keys error: key={}", key, e);
            return 0;
        }
    }

    /**
     * 获取Hash中的所有键值对
     * @param key Redis键
     * @return 所有键值对
     */
    public Map<Object, Object> getHashEntries(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis get hash entries error: key={}", key, e);
            return new HashMap<>();
        }
    }

    /**
     * 判断Hash中是否存在键
     * @param key Redis键
     * @param hashKey Hash的键
     * @return 是否存在
     */
    public boolean hasHashKey(String key, Object hashKey) {
        try {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        } catch (Exception e) {
            log.error("Redis check hash key exists error: key={}, hashKey={}", key, hashKey, e);
            return false;
        }
    }

    /**
     * 原子递增操作
     * @param key Redis键
     * @param delta 增量
     * @return 递增后的值
     */
    public long increment(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().increment(key, delta);
            return value == null ? 0 : value;
        } catch (Exception e) {
            log.error("Redis increment error: key={}, delta={}", key, delta, e);
            throw new RuntimeException("Redis operation failed", e);
        }
    }
}
