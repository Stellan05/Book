package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.baozi.book.entity.ReasonTemplate;

import java.util.List;

/**
 * 理由模板数据访问层接口
 */
@Mapper
public interface ReasonTemplateMapper extends BaseMapper<ReasonTemplate> {

    /**
     * 根据类型查询理由模板列表
     * @param type 理由类型
     * @param creatorId 创建者ID
     * @return 理由模板列表
     */
    List<ReasonTemplate> getTemplatesByType(@Param("type") Integer type, @Param("creatorId") String creatorId);
} 