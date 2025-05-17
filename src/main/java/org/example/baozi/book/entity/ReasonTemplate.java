package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 理由模板实体类
 */
@Data
@TableName("reason_template")
public class ReasonTemplate {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 创建者ID（管理员用户名）
     */
    private String creatorId;
    
    /**
     * 理由内容
     */
    private String content;
    
    /**
     * 理由类型：1-举报处理理由，2-扣除信誉分理由，3-书籍审核不通过理由
     */
    private Integer type;


} 