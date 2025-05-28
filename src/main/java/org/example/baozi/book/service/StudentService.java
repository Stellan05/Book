package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.Student;

import java.time.LocalDateTime;

/**
 * 学生服务接口
 */
public interface StudentService extends IService<Student> {
    /**
     * 根据学号查询学生信息
     * @param studentId 学号
     * @return 学生信息
     */
    Student getStudentById(String studentId);
    
    /**
     * 更新学生初始信息(手机号、校区、宿舍地址、默认收款方式)
     * @param student 学生对象
     * @return 是否更新成功
     */
    boolean updateStudentProfile(Student student);
    
    /**
     * 创建新学生
     * @param userId 用户ID
     * @param studentId 学号
     * @return 创建的学生对象
     */
    Student createStudent(Integer userId, String studentId);
    
    /**
     * 检查学生是否首次登录
     * @param studentId 学号
     * @return 是否首次登录
     */
    boolean isFirstLogin(String studentId);
    
    /**
     * 更新学生首次登录状态
     * @param studentId 学号
     * @param isFirstLogin 是否首次登录
     * @return 是否更新成功
     */
    boolean updateFirstLoginStatus(String studentId, boolean isFirstLogin);

    /**
     * 更新学生信息
     * @param student 学生对象
     */
    void updateStudent(Student student);
    
    /**
     * 扣除学生信誉积分
     * @param studentId 学号
     * @param points 扣除的积分数量
     * @param reason 扣除原因
     * @return 扣除后的信誉积分
     */
    int deductReputationScore(String studentId, int points, String reason);
    
    /**
     * 增加学生信誉积分
     * @param studentId 学号
     * @param points 增加的积分数量
     * @param reason 增加原因
     * @return 增加后的信誉积分
     */
    int increaseReputationScore(String studentId, int points, String reason);
    
    /**
     * 获取学生当前信誉积分
     * @param studentId 学号
     * @return 信誉积分
     */
    int getReputationScore(String studentId);
    
    /**
     * 检查学生是否被禁用
     * @param studentId 学号
     * @return 是否被禁用
     */
    boolean isDisabled(String studentId);
    
    /**
     * 禁用学生账号
     * @param studentId 学号
     * @param endTime 禁用结束时间
     * @param reason 禁用原因
     * @return 是否禁用成功
     */
    boolean disableStudent(String studentId, LocalDateTime endTime, String reason);
    
    /**
     * 启用学生账号
     * @param studentId 学号
     * @return 是否启用成功
     */
    boolean enableStudent(String studentId);
}