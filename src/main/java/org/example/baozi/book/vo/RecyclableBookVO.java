package org.example.baozi.book.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.baozi.book.entity.Book;

import java.time.LocalDateTime;

/**
 * 作为一个 VO 类--给view层返回需要的数据
 */
@Data
@AllArgsConstructor
public class RecyclableBookVO {
    // Book 的基本信息
    private Integer id;
    private String ownerId;
    private String campus;
    private LocalDateTime createTime;
    @JsonIgnore  // 忽略空值序列化
    private byte[] bookData;
    // RecyclingBook 的信息
    private String title;
    private String publisher;
    private String edition;

}
