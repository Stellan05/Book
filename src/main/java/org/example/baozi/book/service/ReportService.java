package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.Report;
import org.example.baozi.book.response.PageResult;

import java.util.List;

/**
 * 举报信息服务接口
 */
public interface ReportService extends IService<Report> {
    /**
     * 提交举报
     * @param reporterId 举报者ID
     * @param bookId 书籍ID
     * @param reason 举报原因
     * @return 是否提交成功
     */
    boolean submitReport(String reporterId, Long bookId, String reportedId, String reason,Integer type);
    
    /**
     * 分页查询举报信息
     * @param page 页码
     * @param size 每页大小
     * @param status 处理状态
     * @return 分页结果
     */
    PageResult<Report> getReportPage(Integer page, Integer size, Integer status);
    
    /**
     * 处理举报信息
     * @param id 举报ID
     * @param result 处理结果
     * @param handlerId 处理人ID
     * @param opinion 处理意见
     * @param deductCredit 是否扣除信誉积分
     * @param diffScore 需要扣除的信誉积分
     * @return 是否处理成功
     */
    boolean handleReport(Integer id, Integer result, Integer handlerId, String opinion, boolean deductCredit,Integer diffScore);
    
    /**
     * 批量处理举报信息
     * @param ids 举报ID列表
     * @param result 处理结果
     * @param handlerId 处理人ID
     * @param opinion 处理意见
     * @param deductCredit 是否扣除信誉积分
     * @return 是否处理成功
     */
    boolean batchHandleReport(List<Integer> ids, Integer result, String handlerId, String opinion, boolean deductCredit);
    
    /**
     * 撤销举报处理
     * @param id 举报ID
     * @param handlerId 处理人ID
     * @return 是否撤销成功
     */
    boolean cancelReport(Integer id, Integer handlerId);
    
    /**
     * 获取举报详情
     * @param id 举报ID
     * @return 举报详情
     */
    Report getReportDetail(Integer id);
} 