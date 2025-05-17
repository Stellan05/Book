package org.example.baozi.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 可回收利用的书籍实体类
 */
@Data
@TableName("recyclable_book")
public class RecyclableBook {
    /**
     * 主键ID
     */
    @TableId(value = "r_id", type = IdType.AUTO)
    private Integer rId;
    
    /**
     * 关联书籍ID
     */
    private Long bookId;
    
    /**
     * 书名
     */
    private String bookTitle;
    
    /**
     * 印刷版次
     */
    private String printingEdition;
    
    /**
     * 出版商
     */
    private String publisher;
} 