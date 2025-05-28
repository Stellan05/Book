package org.example.baozi.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.Collector;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.AuthService;
import org.example.baozi.book.service.CollectorService;
import org.example.baozi.book.service.UserService;
import org.example.baozi.book.util.JWTUtil;
import org.example.baozi.book.vo.CollectOrderVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 收书员控制器
 */
@RestController
@RequestMapping("/collector")
@RequiredArgsConstructor
public class CollectorController {

    private final CollectorService collectorService;
    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    
    /**
     * 获取收书员个人信息
     * @param request HTTP请求
     * @return 收书员信息
     */
    @GetMapping("/profile")
    public ResponseMessage<?> getProfile(HttpServletRequest request) {
        String token = JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        Collector collector = collectorService.getCollectorById(collectorId);
        if (collector == null) {
            return ResponseMessage.error("收书员信息不存在");
        }
        
        return ResponseMessage.success(collector);
    }
    
    /**
     * 更新收书员个人信息
     * @param request HTTP请求
     * @param profileRequest 更新信息
     * @return 更新结果
     */
    @PutMapping("/profile")
    public ResponseMessage<?> updateProfile(HttpServletRequest request, @RequestBody Map<String, String> profileRequest) {
        String token = JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        Collector collector = collectorService.getCollectorById(collectorId);
        if (collector == null) {
            return ResponseMessage.error("收书员信息不存在");
        }
        
        // 更新信息
        String phone = profileRequest.get("phone");
        String campus = profileRequest.get("campus");
        String paymentMethod = profileRequest.get("paymentMethod");
        
        if (phone != null) {
            collector.setPhone(phone);
        }
        
        if (campus != null) {
            collector.setCampus(campus);
        }
        
        if (paymentMethod != null) {
            collector.setPaymentMethod(paymentMethod);
        }
        
        boolean success = collectorService.updateCollectorInfo(collector);
        
        if (success) {
            return ResponseMessage.success("更新信息成功");
        } else {
            return ResponseMessage.error("更新信息失败");
        }
    }
    
    /**
     * 更新密码
     * @param request HTTP请求
     * @param passwordRequest 密码信息
     * @return 更新结果
     */
    @PutMapping("/password")
    public ResponseMessage<?> updatePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordRequest) {
        String token = JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseMessage.error("请提供旧密码和新密码");
        }
        
        User user = userService.getUserByUsername(collectorId);
        if (user == null) {
            return ResponseMessage.error("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseMessage.error("旧密码不正确");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        
        return ResponseMessage.success("密码修改成功");
    }
    
    /**
     * 获取校区订单列表
     * @param request HTTP请求
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    @GetMapping("/orders/campus")
    public ResponseMessage<?> getCampusOrders(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        String token =  JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        Collector collector = collectorService.getCollectorById(collectorId);
        if (collector == null) {
            return ResponseMessage.error("收书员信息不存在");
        }
        
        // 获取收书员所在校区的订单
        PageResult<CollectOrderVO> orders = collectorService.getOrdersByCampus(collector.getCampus(), page, size);
        
        return ResponseMessage.success(orders);
    }
    
    /**
     * 收书员接单
     * @param request HTTP请求
     * @param orderId 订单ID
     * @return 接单结果
     */
    @PostMapping("/orders/{orderId}/accept")
    public ResponseMessage<?> acceptOrder(HttpServletRequest request, @PathVariable Integer orderId) {
        String token = JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        boolean success = collectorService.acceptOrder(collectorId, orderId);
        
        if (success) {
            return ResponseMessage.success("接单成功");
        } else {
            return ResponseMessage.error("接单失败，可能订单不存在或已被接单");
        }
    }
    
    /**
     * 上传书籍照片和实际重量完成订单
     * @param orderId 订单ID
     * @param actualWeight 实际重量
     * @return 完成结果
     */
    @PostMapping("/orders/{orderId}/complete")
    public ResponseMessage<?> completeOrder(
            @PathVariable Integer orderId,
            @RequestParam("actualWeight") Double actualWeight,
            @RequestParam(value = "actualPrice",required = false) Double actualPrice
    )
    {

        
        boolean success = collectorService.completeOrder(orderId, actualWeight,actualPrice);
        
        if (success) {
            return ResponseMessage.success("订单完成成功");
        } else {
            return ResponseMessage.error("订单完成失败，可能订单不存在或状态错误");
        }
    }
    
    /**
     * 获取收书员已接订单列表
     * @param request HTTP请求
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    @GetMapping("/orders/my")
    public ResponseMessage<?> getMyOrders(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String collectorId = JWTUtil.getUsernameFromToken(token);
        
        PageResult<CollectOrderVO> orders = collectorService.getCollectorOrders(collectorId, page, size);
        
        return ResponseMessage.success(orders);
    }
}