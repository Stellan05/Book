package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.baozi.book.entity.Book;

import java.util.List;

/**
 * Book表数据访问层接口
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
} 