package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.baozi.book.entity.SealedBook;
import org.example.baozi.book.vo.SealedBookVO;

import java.util.List;

/**
 * SealedBook表数据访问层接口
 */
@Mapper
public interface SealedBookMapper extends BaseMapper<SealedBook> {
    List<SealedBookVO> sealedBookList(@Param("ownerId") String ownerId);
} 