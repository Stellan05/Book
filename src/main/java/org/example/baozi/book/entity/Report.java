package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报信息实体类
 */
@Data
@TableName("report")
public class Report {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 举报者ID（学生ID）
     */
    private String reporterId;

    /**
     * 被举报者ID
     */
    private String reportedId;
    
    /**
     * 被举报书籍ID
     */
    private Long bookId;
    
    /**
     * 举报原因
     */
    private String reason;
    
    /**
     * 举报时间
     */
    @TableField(fill = FieldFill.INSERT,value = "report_time")
    private LocalDateTime createTime;
    
    /**
     * 处理状态：0-未处理，1-已处理，2-已撤销
     */
    private Integer status;
    
    /**
     * 处理结果：0-无效举报，1-有效举报
     */
    private Integer result;
    
    /**
     * 处理时间
     */
    @TableField(fill = FieldFill.UPDATE,value = "handle_time")
    private LocalDateTime updateTime;
    
    /**
     * 处理人ID（管理员用户名）
     */
    private Integer handlerId;
    
    /**
     * 处理意见
     */
    private String opinion;

    /**
     * 被举报书籍类型 0--SealedBook 1--RecyclingBook
     */
    private Integer bookType;

    /**
     * 被扣除的信誉分
     */
    private Integer diffScore;
} 