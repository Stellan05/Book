package org.example.baozi.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.baozi.book.entity.Report;


import java.util.List;

/**
 * 举报信息数据访问层接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    /**
     * 分页查询举报信息
     * @param page 分页参数
     * @param status 处理状态
     * @return 举报信息VO分页结果
     */
    Page<Report> getReportPage(Page<Report> page, @Param("status") Integer status);
    
    /**
     * 批量更新举报信息状态
     * @param ids 举报信息ID列表
     * @param status 状态
     * @param result 处理结果
     * @param handlerId 处理人ID
     * @param opinion 处理意见
     * @return 更新记录数
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, 
                          @Param("status") Integer status, 
                          @Param("result") Integer result, 
                          @Param("handlerId") String handlerId,
                          @Param("opinion") String opinion);

} 