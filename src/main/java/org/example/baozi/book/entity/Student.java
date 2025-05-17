package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学生实体类
 */
@Data
@TableName("student")
public class Student {

    /**
     * 学号--即主键
     */
    @TableId
    private String studentId;
    
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 校区
     */
    private String campus;
    
    /**
     * 宿舍地址
     */
    private String dormitory;
    
    /**
     * 信誉分
     */
    private Integer reputationScore;
    
    /**
     * 状态（1：正常，0：禁用）
     */
    private Boolean status;
    
    /**
     * 默认收款方式
     */
    private String paymentMethod;
    
    /**
     * 是否首次登录（1：是，0：否）
     */
    private Boolean firstLogin;
} 