package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService extends IService<Admin> {
    /**
     * 创建管理员
     * @param userId 用户ID
     * @param adminId 管理员ID
     * @return 是否创建成功
     */
    boolean createAdmin(Integer userId, Long adminId);
    
    /**
     * 根据用户名获取管理员信息
     * @param username 用户名
     * @return 管理员信息
     */
    Admin getAdminByUsername(String username);
    
    /**
     * 检查是否为书籍审核管理员
     * @param username 用户名
     * @return 是否为书籍审核管理员
     */
    boolean isReportAdmin(String username);
    
    /**
     * 检查是否为学生管理员
     * @param username 用户名
     * @return 是否为学生管理员
     */
    boolean isStudentAdmin(String username);
} 