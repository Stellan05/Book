package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 需要卖的书籍实体类
 */
@Data
@TableName("sealed_book")
public class SealedBook {
    /**
     * 主键ID
     */
    @TableId(value = "s_id", type = IdType.AUTO)
    private Integer sId;
    
    /**
     * 关联书籍ID
     */
    private Long bookId;
    
    /**
     * 书籍重量
     */
    private Double bookWeight;
    
    /**
     * 价格（1.6*重量）
     */
    private Double price;
    
    /**
     * 是否被收书员接单（1:接单 0:未接单）
     */
    private Boolean isAccept;
} 