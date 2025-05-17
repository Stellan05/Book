package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理员信息实体类
 */
@Data
@TableName("admin")
public class Admin {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    
    /**
     * 关联用户表ID
     */
    private Integer userId;
}