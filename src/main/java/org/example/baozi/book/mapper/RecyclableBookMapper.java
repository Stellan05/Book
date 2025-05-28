package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.example.baozi.book.entity.RecyclableBook;
import org.example.baozi.book.vo.RecyclableBookVO;

/**
 * RecyclableBook表数据访问层接口
 */
@Mapper
public interface RecyclableBookMapper extends BaseMapper<RecyclableBook> {
    Page<RecyclableBookVO> getRBookByPage(Page<?> page);
} 