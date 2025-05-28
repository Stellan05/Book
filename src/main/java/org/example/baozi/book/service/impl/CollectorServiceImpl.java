package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.CollectOrder;
import org.example.baozi.book.entity.Collector;
import org.example.baozi.book.entity.SealedBook;
import org.example.baozi.book.mapper.CollectOrderMapper;
import org.example.baozi.book.mapper.CollectorMapper;
import org.example.baozi.book.mapper.SealedBookMapper;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.service.CollectorService;
import org.example.baozi.book.vo.CollectOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 收书员服务接口实现类
 */
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl extends ServiceImpl<CollectorMapper, Collector> implements CollectorService {

    private final CollectorMapper collectorMapper;
    private final CollectOrderMapper collectOrderMapper;
    private final SealedBookMapper sealedBookMapper;

    /**
     * 根据学号查询收书员信息
     *
     * @param collectorId 学号
     * @return 收书员信息
     */
    @Override
    public Collector getCollectorById(String collectorId) {
        return getById(collectorId);
    }

    /**
     * 注册收书员
     *
     * @param userId     用户ID
     * @param collectorId 学号
     * @param realName   真实姓名
     * @param phone      联系电话
     * @param campus     校区
     * @return 创建的收书员对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Collector registerCollector(Integer userId, String collectorId, String realName, String phone, String campus,String payment) {
        // 创建收书员对象
        Collector collector = new Collector();
        collector.setCollectorId(collectorId);
        collector.setUserId(userId);
        collector.setRealName(realName);
        collector.setPhone(phone);
        collector.setCampus(campus);
        collector.setPaymentMethod(payment);
        collector.setStatus(true); // 设置状态为正常
        collector.setOrderCount(0); // 初始接单数为0
        
        // 保存收书员信息
        save(collector);
        
        return collector;
    }

    /**
     * 更新收书员信息
     *
     * @param collector 收书员对象
     * @return 是否更新成功
     */
    @Override
    public boolean updateCollectorInfo(Collector collector) {
        // 只允许更新手机号、校区和支付方式
        Collector existingCollector = getById(collector.getCollectorId());
        if (existingCollector == null) {
            return false;
        }
        
        existingCollector.setPhone(collector.getPhone());
        existingCollector.setCampus(collector.getCampus());
        existingCollector.setPaymentMethod(collector.getPaymentMethod());
        
        return updateById(existingCollector);
    }

    /**
     * 更新收书员支付方式
     *
     * @param collectorId   收书员ID
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    @Override
    public boolean updatePaymentMethod(String collectorId, String paymentMethod) {
        Collector collector = getById(collectorId);
        if (collector == null) {
            return false;
        }
        
        collector.setPaymentMethod(paymentMethod);
        return updateById(collector);
    }

    /**
     * 根据校区获取订单列表
     *
     * @param campus 校区
     * @param page   页码
     * @param size   每页大小
     * @return 订单分页列表
     */
    @Override
    public PageResult<CollectOrderVO> getOrdersByCampus(String campus, Integer page, Integer size) {
        Page<CollectOrder> pageParam = new Page<>(page, size);
        Page<CollectOrderVO> resultPage = collectOrderMapper.getOrderPageByCampus(pageParam, campus);
        
        return new PageResult<>(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getSize(),
                resultPage.getCurrent()
        );
    }

    /**
     * 收书员接单
     *
     * @param collectorId 收书员ID
     * @param orderId     订单ID
     * @return 是否接单成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(String collectorId, Integer orderId) {
        // 查询订单是否存在
        CollectOrder order = collectOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        
        // 更新订单信息
        order.setCollectorId(collectorId);
        order.setStatus(1); // 设置为已接单
        order.setAcceptTime(LocalDateTime.now());;
        
        // 更新收书员接单数
        Collector collector = getById(collectorId);
        collector.setOrderCount(collector.getOrderCount() + 1);
        updateById(collector);

        // 更新售卖图书接单信息
        SealedBook sealedBook = sealedBookMapper.selectById(order.getSealedBookId());
        if (sealedBook == null) {
            return false;
        }
        sealedBook.setIsAccept(true);
        
        return collectOrderMapper.updateById(order) > 0 && sealedBookMapper.updateById(sealedBook) > 0;
    }

    /**
     * 收书员完成订单
     * @param orderId      订单ID
     * @param actualWeight 实际重量
     * @param actualPrice 实际每斤价钱
     * @return 是否完成成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Integer orderId, Double actualWeight,Double actualPrice) {
        // 查询订单是否存在
        CollectOrder order = collectOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        if(actualPrice!=null)
            order.setPricePerKg(actualPrice);
        // 计算金额
        double totalAmount = actualWeight * order.getPricePerKg();
        double commissionAmount = totalAmount * 0.2;
        double studentAmount = totalAmount - commissionAmount;
        
        // 更新订单信息
        order.setStatus(2); // 设置为已完成
        order.setFinishTime(LocalDateTime.now());
        order.setActualWeight(actualWeight);
        order.setCommissionAmount(commissionAmount);
        order.setStudentAmount(studentAmount);
        
        return collectOrderMapper.updateById(order) > 0;
    }

    /**
     * 获取收书员已接订单列表
     *
     * @param collectorId 收书员ID
     * @param page        页码
     * @param size        每页大小
     * @return 订单分页列表
     */
    @Override
    public PageResult<CollectOrderVO> getCollectorOrders(String collectorId, Integer page, Integer size) {
        Page<CollectOrder> pageParam = new Page<>(page, size);
        Page<CollectOrderVO> resultPage = collectOrderMapper.getOrderPageByCollector(pageParam, collectorId);
        
        return new PageResult<>(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getSize(),
                resultPage.getCurrent()
        );
    }
}