package org.example.baozi.book.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收书订单视图对象
 */
@Data
public class CollectOrderVO {
    /**
     * 订单ID
     */
    private Integer id;
    
    /**
     * 学生ID
     */
    private String studentId;

    
    /**
     * 学生联系电话
     */
    private String studentPhone;
    
    /**
     * 学生宿舍地址
     */
    private String studentDormitory;
    
    /**
     * 收书员ID
     */
    private String collectorId;
    
    /**
     * 收书员姓名
     */
    private String collectorName;
    
    /**
     * 收书员联系电话
     */
    private String collectorPhone;
    
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
     * 佣金金额
     */
    private Double commissionAmount;
    
    /**
     * 学生所得金额
     */
    private Double studentAmount;
    
    
}