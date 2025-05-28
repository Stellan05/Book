package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.mapper.StudentMapper;
import org.example.baozi.book.service.StudentService;
import org.example.baozi.book.service.TokenService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 学生服务实现类
 * 负责学生信息管理，包括信誉积分管理和账号状态管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final StudentMapper studentMapper;
    private final RedisServiceImpl redisService;
    private final TokenService tokenService;
    
    // Redis键前缀
    private static final String STUDENT_KEY = RedisServiceImpl.KEY_PREFIX_STUDENT;
    private static final String REPUTATION_KEY = STUDENT_KEY + "reputation:";
    private static final String STATUS_KEY = STUDENT_KEY + "status:";
    private static final String BAN_KEY = STUDENT_KEY + "ban:";
    private static final long CACHE_EXPIRE = 24 * 60 * 60;
    
    // 信誉积分相关常量
    private static final int MAX_REPUTATION = 100;
    private static final int MIN_REPUTATION = 0;
    private static final int DISABLE_THRESHOLD = 60; // 禁用阈值，低于此值将被禁用

    /**
     * 根据学号查询学生
     * @param studentId 学号
     * @return 学生信息
     */
    @Override
    @Cacheable(value = "student", key = "#studentId", unless = "#result == null")   // 返回的result为空则不缓存
    public Student getStudentById(String studentId) {
        // 先从Redis缓存中获取
        String key = STUDENT_KEY + studentId;
        Object cachedStudent = redisService.getValue(key);
        
        if (cachedStudent != null) {
            log.debug("从Redis缓存获取学生信息: {}", studentId);
            return (Student) cachedStudent;
        }
        
        // 缓存未命中，从数据库查询
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentId, studentId);
        Student student = getOne(queryWrapper);
        
        // 如果学生存在，缓存到Redis
        if (student != null) {
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
        }
        
        return student;
    }
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 是否更新成功
     */
    @Override
    @CachePut(value = "student", key = "#student.studentId")
    public boolean updateStudentProfile(Student student) {
        boolean result = updateById(student);
        if (result) {
            // 更新Redis缓存
            String key = STUDENT_KEY + student.getStudentId();
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 清除相关缓存
            redisService.deleteValue(REPUTATION_KEY + student.getStudentId());
            redisService.deleteValue(STATUS_KEY + student.getStudentId());
        }
        return result;
    }
    
    /**
     * 创建学生
     * @param userId 用户ID
     * @param studentId 学号
     * @return 创建的学生
     */
    @Override
    @Transactional
    public Student createStudent(Integer userId, String studentId) {
        Student student = new Student();
        student.setUserId(userId);
        student.setStudentId(studentId);
        student.setFirstLogin(true); // 设置为首次登录
        student.setReputationScore(MAX_REPUTATION); // 初始信誉分
        student.setStatus(true); // 正常状态
        
        boolean saved = save(student);
        if (saved) {
            // 缓存到Redis
            String key = STUDENT_KEY + studentId;
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 保存信誉积分记录
            Map<String, Object> scoreRecord = new HashMap<>();
            scoreRecord.put("score", MAX_REPUTATION);
            scoreRecord.put("time", LocalDateTime.now());
            scoreRecord.put("reason", "新用户初始积分");
            scoreRecord.put("operation", "init");
            
            String reputationKey = REPUTATION_KEY + studentId + ":history";
            redisService.putHash(reputationKey, LocalDateTime.now().toString(), scoreRecord);
        }
        
        return student;
    }
    
    /**
     * 检查学生是否首次登录
     * @param studentId 学号
     * @return 是否首次登录
     */
    @Override
    public boolean isFirstLogin(String studentId) {
        Student student = getStudentById(studentId);
        return student != null && student.getFirstLogin();
    }
    
    /**
     * 更新学生首次登录状态
     * @param studentId 学号
     * @param isFirstLogin 是否首次登录
     * @return 是否更新成功
     */
    @Override
    @CacheEvict(value = "student", key = "#studentId")
    public boolean updateFirstLoginStatus(String studentId, boolean isFirstLogin) {
        Student student = getStudentById(studentId);
        if (student == null) {
            return false;
        }
        
        student.setFirstLogin(isFirstLogin);
        boolean result = updateById(student);
        
        if (result) {
            // 更新Redis缓存
            String key = STUDENT_KEY + studentId;
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
        }
        
        return result;
    }

    /**
     * 更新学生信息
     * @param student 学生对象
     */
    @Override
    @CacheEvict(value = "student", key = "#student.studentId")
    public void updateStudent(Student student) {
        boolean result = updateById(student);
        if (result) {
            // 更新Redis缓存
            String key = STUDENT_KEY + student.getStudentId();
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 清除信誉积分缓存
            redisService.deleteValue(REPUTATION_KEY + student.getStudentId());
            
            // 如果信誉积分低于阈值，考虑禁用账号
            if (student.getReputationScore() < DISABLE_THRESHOLD) {
                disableStudent(student.getStudentId(), LocalDateTime.now().plusMonths(3), "信誉积分过低");
            }
        }
    }
    
    /**
     * 扣除学生信誉积分
     * @param studentId 学号
     * @param points 扣除的积分数量
     * @param reason 扣除原因
     * @return 扣除后的信誉积分
     */
    @Override
    @Transactional
    public int deductReputationScore(String studentId, int points, String reason) {
        // 获取学生信息
        Student student = getStudentById(studentId);
        if (student == null) {
            log.warn("尝试扣除不存在的学生积分: {}", studentId);
            return -1;
        }
        
        // 计算新的信誉积分
        int currentScore = student.getReputationScore();
        int newScore = Math.max(MIN_REPUTATION, currentScore - points);
        student.setReputationScore(newScore);
        
        // 更新数据库
        updateStudent(student);
        
        // 记录积分变更记录
        Map<String, Object> scoreRecord = new HashMap<>();
        scoreRecord.put("score", newScore);
        scoreRecord.put("change", -points);
        scoreRecord.put("time", LocalDateTime.now());
        scoreRecord.put("reason", reason);
        scoreRecord.put("operation", "deduct");
        
        String reputationKey = REPUTATION_KEY + studentId + ":history";
        redisService.putHash(reputationKey, LocalDateTime.now().toString(), scoreRecord);
        
        // 更新积分缓存
        redisService.setValue(REPUTATION_KEY + studentId, newScore);
        
        // 如果信誉积分低于阈值，禁用账号三个月
        if (newScore < DISABLE_THRESHOLD) {
            log.info("学生信誉积分过低，禁用账号: {}, 分数: {}", studentId, newScore);
            disableStudent(studentId, LocalDateTime.now().plusMonths(3), "信誉积分过低");
        }
        
        return newScore;
    }
    
    /**
     * 增加学生信誉积分
     * @param studentId 学号
     * @param points 增加的积分数量
     * @param reason 增加原因
     * @return 增加后的信誉积分
     */
    @Override
    @Transactional
    public int increaseReputationScore(String studentId, int points, String reason) {
        // 获取学生信息
        Student student = getStudentById(studentId);
        if (student == null) {
            log.warn("尝试增加不存在的学生积分: {}", studentId);
            return -1;
        }
        
        // 计算新的信誉积分
        int currentScore = student.getReputationScore();
        int newScore = Math.min(MAX_REPUTATION, currentScore + points);
        student.setReputationScore(newScore);
        
        // 更新数据库
        updateStudent(student);
        
        // 记录积分变更记录
        Map<String, Object> scoreRecord = new HashMap<>();
        scoreRecord.put("score", newScore);
        scoreRecord.put("change", points);
        scoreRecord.put("time", LocalDateTime.now());
        scoreRecord.put("reason", reason);
        scoreRecord.put("operation", "increase");
        
        String reputationKey = REPUTATION_KEY + studentId + ":history";
        redisService.putHash(reputationKey, LocalDateTime.now().toString(), scoreRecord);
        
        // 更新积分缓存
        redisService.setValue(REPUTATION_KEY + studentId, newScore);
        
        // 如果信誉积分恢复到阈值以上，考虑解禁账号
        if (newScore >= DISABLE_THRESHOLD && !student.getStatus()) {
            log.info("学生信誉积分已恢复，启用账号: {}, 分数: {}", studentId, newScore);
            enableStudent(studentId);
        }
        
        return newScore;
    }
    
    /**
     * 获取学生当前信誉积分
     * @param studentId 学号
     * @return 信誉积分
     */
    @Override
    public int getReputationScore(String studentId) {
        // 先从Redis缓存获取
        String key = REPUTATION_KEY + studentId;
        Object cachedScore = redisService.getValue(key);
        
        if (cachedScore != null) {
            return (int) cachedScore;
        }
        
        // 缓存未命中，从数据库获取
        Student student = getStudentById(studentId);
        if (student == null) {
            return -1;
        }
        
        // 缓存积分
        redisService.setValue(key, student.getReputationScore());
        
        return student.getReputationScore();
    }
    
    /**
     * 检查学生是否被禁用
     * @param studentId 学号
     * @return 是否被禁用
     */
    @Override
    public boolean isDisabled(String studentId) {
        // 先检查Redis中的禁用记录
        String banKey = BAN_KEY + studentId;
        Object banInfo = redisService.getValue(banKey);
        
        if (banInfo != null) {
            // 禁用时间还未到期
            return true;
        }
        
        // 检查学生状态
        Student student = getStudentById(studentId);
        if (student == null) {
            return false;
        }
        
        return !student.getStatus(); // status为false表示禁用
    }
    
    /**
     * 禁用学生账号
     * @param studentId 学号
     * @param endTime 禁用结束时间
     * @param reason 禁用原因
     * @return 是否禁用成功
     */
    @Override
    @Transactional
    public boolean disableStudent(String studentId, LocalDateTime endTime, String reason) {
        // 获取学生信息
        Student student = getStudentById(studentId);
        if (student == null) {
            log.warn("尝试禁用不存在的学生账号: {}", studentId);
            return false;
        }
        
        // 更新学生状态
        student.setStatus(false); // 设置为禁用
        boolean result = updateById(student);
        
        if (result) {
            // 更新Redis缓存
            String key = STUDENT_KEY + studentId;
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 记录禁用信息
            Map<String, Object> banInfo = new HashMap<>();
            banInfo.put("endTime", endTime);
            banInfo.put("reason", reason);
            banInfo.put("startTime", LocalDateTime.now());
            
            String banKey = BAN_KEY + studentId;
            redisService.setValueWithExpire(banKey, banInfo, 
                    java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds(), 
                    TimeUnit.SECONDS);
            
            // 更新状态缓存
            redisService.setValue(STATUS_KEY + studentId, false);
            
            // 禁用该用户的所有Token
            Integer userId = student.getUserId();
            if (userId != null) {
                tokenService.disableUserTokens(userId, "账号已被禁用: " + reason, 
                        java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds(), 
                        TimeUnit.SECONDS);
            }
        }
        
        return result;
    }
    
    /**
     * 启用学生账号
     * @param studentId 学号
     * @return 是否启用成功
     */
    @Override
    @Transactional
    public boolean enableStudent(String studentId) {
        // 获取学生信息
        Student student = getStudentById(studentId);
        if (student == null) {
            log.warn("尝试启用不存在的学生账号: {}", studentId);
            return false;
        }
        
        // 更新学生状态
        student.setStatus(true); // 设置为正常
        boolean result = updateById(student);
        
        if (result) {
            // 更新Redis缓存
            String key = STUDENT_KEY + studentId;
            redisService.setValueWithExpire(key, student, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 移除禁用记录
            String banKey = BAN_KEY + studentId;
            redisService.deleteValue(banKey);
            
            // 更新状态缓存
            redisService.setValue(STATUS_KEY + studentId, true);
        }
        
        return result;
    }
}