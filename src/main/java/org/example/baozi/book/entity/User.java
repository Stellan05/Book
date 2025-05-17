package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户基本信息实体类
 */
@Data
@TableName("user")
public class User {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 用户账号（学生学号或收书员、管理员账号）
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 用户权限集合
     */
    private String roles;
} 