package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.CollectOrder;
import org.example.baozi.book.entity.Collector;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.vo.CollectOrderVO;

/**
 * 收书员服务接口
 */
public interface CollectorService extends IService<Collector> {
    
    /**
     * 根据学号查询收书员信息
     * @param collectorId 学号
     * @return 收书员信息
     */
    Collector getCollectorById(String collectorId);
    
    /**
     * 注册收书员
     * @param userId 用户ID
     * @param collectorId 学号
     * @param realName 真实姓名
     * @param phone 联系电话
     * @param campus 校区
     * @return 创建的收书员对象
     */
    Collector registerCollector(Integer userId, String collectorId, String realName, String phone, String campus,String payment);
    
    /**
     * 更新收书员信息
     * @param collector 收书员对象
     * @return 是否更新成功
     */
    boolean updateCollectorInfo(Collector collector);
    
    /**
     * 更新收书员支付方式
     * @param collectorId 收书员ID
     * @param paymentMethod 支付方式
     * @return 是否更新成功
     */
    boolean updatePaymentMethod(String collectorId, String paymentMethod);
    
    /**
     * 根据校区获取订单列表
     * @param campus 校区
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    PageResult<CollectOrderVO> getOrdersByCampus(String campus, Integer page, Integer size);
    
    /**
     * 收书员接单
     * @param collectorId 收书员ID
     * @param orderId 订单ID
     * @return 是否接单成功
     */
    boolean acceptOrder(String collectorId, Integer orderId);
    
    /**
     * 收书员完成订单
     * @param orderId 订单ID
     * @param actualWeight 实际重量
     * @return 是否完成成功
     */
    boolean completeOrder(Integer orderId, Double actualWeight,Double actualPrice);
    
    /**
     * 获取收书员已接订单列表
     * @param collectorId 收书员ID
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    PageResult<CollectOrderVO> getCollectorOrders(String collectorId, Integer page, Integer size);
}