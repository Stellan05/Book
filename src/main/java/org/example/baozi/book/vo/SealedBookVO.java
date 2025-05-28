package org.example.baozi.book.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SealedBookVO {
    // Book 的基本信息
    private Integer id;
    private String ownerId;
    private String campus;
    private LocalDateTime createTime;
    private byte[] bookData;
    // SealedBook 的信息
    private Double weight;
    private Double price;
    // 检查是否有图片信息
    Boolean hasCover;
}
