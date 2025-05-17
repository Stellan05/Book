package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书籍基本信息实体类
 */
@Data
@TableName("book")
public class Book {
    /**
     * 书的主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 创建时间
     * 用注解自动插入创建时间（以当前时间）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 上传者学号
     */
    private String ownerId;
    
    /**
     * 上传者校区：朝晖、屏峰、莫干山
     */
    private String campus;
    
    /**
     * 书籍信息（图片数据）
     */
    private byte[] bookData;
} 