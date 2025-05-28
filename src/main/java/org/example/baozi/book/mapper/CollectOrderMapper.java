package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.baozi.book.entity.CollectOrder;
import org.example.baozi.book.vo.CollectOrderVO;

/**
 * 收书订单数据访问层接口
 */
@Mapper
public interface CollectOrderMapper extends BaseMapper<CollectOrder> {
    
    /**
     * 根据校区分页查询待接单订单
     * @param page 分页参数
     * @param campus 校区
     * @return 订单列表
     */
    Page<CollectOrderVO> getOrderPageByCampus(Page<CollectOrder> page, @Param("campus") String campus);
    
    /**
     * 根据收书员ID分页查询已接单订单
     * @param page 分页参数
     * @param collectorId 收书员ID
     * @return 订单列表
     */
    Page<CollectOrderVO> getOrderPageByCollector(Page<CollectOrder> page, @Param("collectorId") String collectorId);
    
} 