package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.baozi.book.entity.Collector;

/**
 * 收书员数据访问层接口
 */
@Mapper
public interface CollectorMapper extends BaseMapper<Collector> {
    
} 