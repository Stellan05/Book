package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收书订单实体类
 */
@Data
@TableName("collect_order")
public class CollectOrder {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 收书员ID
     */
    private String collectorId;
    
    /**
     * 书籍ID
     */
    private Integer sealedBookId;
    
    /**
     * 校区
     */
    private String campus;
    
    /**
     * 订单状态(0:待接单 1:已接单 2:已完成 3:已取消)
     */
    private Integer status;
    
    /**
     * 订单创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    
    
    /**
     * 实际重量
     */
    private Double actualWeight;
    
    /**
     * 每公斤价格
     */
    private Double pricePerKg;
    
    
    /**
     * 跑腿费金额
     */
    private Double commissionAmount;
    
    /**
     * 学生所得金额
     */
    private Double studentAmount;
    
    

} 