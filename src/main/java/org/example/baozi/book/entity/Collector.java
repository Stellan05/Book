package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收书员实体类
 */
@Data
@TableName("collector")
public class Collector {

    /**
     * 学号--即主键
     */
    @TableId
    private String collectorId;
    
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 校区（朝晖、屏峰、莫干山）
     */
    private String campus;
    
    /**
     * 状态（1：正常，0：禁用）
     */
    private Boolean status;
    
    /**
     * 默认收款方式
     */
    private String paymentMethod;
    
    /**
     * 累计接单数
     */
    private Integer orderCount;
    

} 