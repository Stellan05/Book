package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.entity.Report;
import org.example.baozi.book.mapper.ReportMapper;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.service.ReportService;
import org.example.baozi.book.service.StudentService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 举报信息服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;
    private final StudentService studentService;
    private final RedisServiceImpl redisService;
    
    // Redis键前缀
    private static final String REPORT_KEY = RedisServiceImpl.KEY_PREFIX_REPORT;
    private static final long CACHE_EXPIRE = 24 * 60 * 60; // 24小时
    
    // 举报结果常量
    private static final int REPORT_INVALID = 0; // 无效举报
    private static final int REPORT_VALID = 1;   // 有效举报
    
    // 举报状态常量
    private static final int STATUS_PENDING = 0; // 未处理
    private static final int STATUS_PROCESSED = 1; // 已处理
    private static final int STATUS_CANCELLED = 2; // 已撤销

    /**
     * 提交举报
     * @param reporterId 举报者ID
     * @param bookId 书籍ID
     * @param reportedId 被举报者ID
     * @param reason 举报原因
     * @param type 举报类型
     * @return 是否提交成功
     */
    @Override
    @Transactional
    public boolean submitReport(String reporterId, Long bookId, String reportedId, String reason, Integer type) {
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setBookId(bookId);
        report.setReportedId(reportedId);
        report.setReason(reason);
        report.setBookType(type);
        report.setStatus(STATUS_PENDING); // 未处理
        report.setCreateTime(LocalDateTime.now()); // 设置创建时间
        
        boolean result = save(report);
        
        if (result) {
            // 缓存举报信息
            String reportKey = REPORT_KEY + report.getId();
            redisService.setValueWithExpire(reportKey, report, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 对于同一本书的举报，缓存到一个集合中，便于快速查询
            String bookReportKey = REPORT_KEY + "book:" + bookId;
            redisService.addToSet(bookReportKey, report.getId());
            
            log.info("用户 {} 举报了书籍 {}, 被举报人 {}, 原因: {}", reporterId, bookId, reportedId, reason);
        }
        
        return result;
    }

    /**
     * 分页查询举报信息
     * @param page 页码
     * @param size 每页大小
     * @param status 处理状态
     * @return 分页结果
     */
    @Override
    //@Cacheable(value = "reports", key = "'page:' + #page + ':size:' + #size + ':status:' + #status", unless = "#result.records.isEmpty()")
    public PageResult<Report> getReportPage(Integer page, Integer size, Integer status) {
        // 设置分页参数
        Page<Report> pageParam = new Page<>(page, size);
        
        // 查询举报分页数据
        Page<Report> reportPage = reportMapper.getReportPage(pageParam, status);
        
        // 构建返回结果
        return new PageResult<>(
                reportPage.getRecords(),
                reportPage.getTotal(),
                reportPage.getSize(),
                reportPage.getCurrent()
        );
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "reports", allEntries = true)
    public boolean handleReport(Integer id, Integer result, Integer handlerId, String opinion, boolean deductCredit, Integer diffScore) {
        // 获取举报信息
        Report report = getById(id);
        if (report == null) {
            log.warn("处理不存在的举报记录: {}", id);
            return false;
        }
        
        // 检查举报是否已处理
        if (report.getStatus() != STATUS_PENDING) {
            log.warn("举报已经被处理或撤销: {}, 当前状态: {}", id, report.getStatus());
            return false;
        }
        
        // 更新举报信息
        report.setStatus(STATUS_PROCESSED); // 已处理
        report.setResult(result);
        report.setHandlerId(handlerId);
        report.setOpinion(opinion);
        report.setUpdateTime(LocalDateTime.now()); // 设置处理时间
        report.setDiffScore(diffScore); // 记录扣除的信誉积分
        
        boolean updateResult = updateById(report);
        
        if (updateResult) {
            // 更新缓存
            String reportKey = REPORT_KEY + id;
            redisService.setValueWithExpire(reportKey, report, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 如果举报有效且需要扣除信誉积分
            if (result == REPORT_VALID && deductCredit) {
                // 扣除被举报人信誉积分
                String reportedId = report.getReportedId();
                if (reportedId != null && !reportedId.isEmpty()) {
                    int newScore = studentService.deductReputationScore(
                            reportedId, 
                            diffScore, 
                            "被举报书籍信息不实，举报ID: " + id
                    );
                    
                    log.info("用户 {} 因举报 {} 被扣除 {} 信誉积分，当前积分: {}", 
                            reportedId, id, diffScore, newScore);
                }
            }
            
            log.info("举报 {} 已被处理, 结果: {}, 处理人: {}", id, result, handlerId);
        }
        
        return updateResult;
    }

    /**
     * 批量处理举报信息
     * @param ids 举报ID列表
     * @param result 处理结果
     * @param handlerId 处理人ID
     * @param opinion 处理意见
     * @param deductCredit 是否扣除信誉积分
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "reports", allEntries = true)
    public boolean batchHandleReport(List<Integer> ids, Integer result, String handlerId, String opinion, boolean deductCredit) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量处理举报时ID列表为空");
            return false;
        }
        
        int handlerIdInt = Integer.parseInt(handlerId);
        
        // 获取所有举报信息
        List<Report> reports = listByIds(ids);
        if (reports.isEmpty()) {
            log.warn("批量处理的举报记录不存在");
            return false;
        }
        
        // 逐个处理举报
        boolean allSuccess = true;
        for (Report report : reports) {
            // 只处理未处理的举报
            if (report.getStatus() == STATUS_PENDING) {
                // 默认扣除10分
                boolean success = handleReport(report.getId(), result, handlerIdInt, opinion, deductCredit, 10);
                if (!success) {
                    allSuccess = false;
                    log.warn("批量处理举报 {} 失败", report.getId());
                }
            }
        }
        
        return allSuccess;
    }

    /**
     * 撤销举报处理
     * @param id 举报ID
     * @param handlerId 处理人ID
     * @return 是否撤销成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "reports", allEntries = true)
    public boolean cancelReport(Integer id, Integer handlerId) {
        // 获取举报信息
        Report report = getById(id);
        if (report == null) {
            log.warn("撤销不存在的举报记录: {}", id);
            return false;
        }
        
        // 检查举报是否已处理且处理结果为有效
        if (report.getStatus() != STATUS_PROCESSED || report.getResult() != REPORT_VALID) {
            log.warn("只能撤销已处理且结果为有效的举报: {}, 当前状态: {}, 结果: {}", 
                    id, report.getStatus(), report.getResult());
            return false;
        }
        
        // 更新举报信息
        report.setStatus(STATUS_CANCELLED); // 已撤销
        report.setUpdateTime(LocalDateTime.now()); // 更新处理时间
        
        boolean updateResult = updateById(report);
        
        if (updateResult) {
            // 更新缓存
            String reportKey = REPORT_KEY + id;
            redisService.setValueWithExpire(reportKey, report, CACHE_EXPIRE, TimeUnit.SECONDS);
            
            // 如果扣除了信誉积分，需要恢复
            Integer diffScore = report.getDiffScore();
            if (diffScore != null && diffScore > 0) {
                String reportedId = report.getReportedId();
                if (reportedId != null && !reportedId.isEmpty()) {
                    // 恢复被举报人信誉积分
                    int newScore = studentService.increaseReputationScore(
                            reportedId, 
                            diffScore, 
                            "举报处理撤销，恢复信誉积分，举报ID: " + id
                    );
                    
                    log.info("用户 {} 因举报 {} 处理撤销，恢复 {} 信誉积分，当前积分: {}", 
                            reportedId, id, diffScore, newScore);
                }
            }
            
            log.info("举报 {} 处理已被撤销, 撤销人: {}", id, handlerId);
        }
        
        return updateResult;
    }

    /**
     * 获取举报详情
     * @param id 举报ID
     * @return 举报详情
     */
    @Override
    @Cacheable(value = "report", key = "#id", unless = "#result == null")
    public Report getReportDetail(Integer id) {
        // 先从Redis缓存获取
        String reportKey = REPORT_KEY + id;
        Object cachedReport = redisService.getValue(reportKey);
        
        if (cachedReport != null) {
            log.debug("从Redis缓存获取举报信息: {}", id);
            return (Report) cachedReport;
        }
        
        // 缓存未命中，从数据库查询
        Report report = getById(id);
        
        // 如果举报存在，缓存到Redis
        if (report != null) {
            redisService.setValueWithExpire(reportKey, report, CACHE_EXPIRE, TimeUnit.SECONDS);
        }
        
        return report;
    }
}