package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.Admin;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.mapper.AdminMapper;
import org.example.baozi.book.service.AdminService;
import org.example.baozi.book.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final UserService userService;

    /**
     * 创建管理员
     * @param userId 用户ID
     * @param adminId 管理员ID
     * @return 是否创建成功
     */
    @Override
    public boolean createAdmin(Integer userId, Long adminId) {
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setUserId(userId);
        return save(admin);
    }

    /**
     * 根据用户名获取管理员信息
     * @param username 用户名
     * @return 管理员信息
     */
    @Override
    public Admin getAdminByUsername(String username) {
        // 先根据用户名查询用户信息
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        
        // 再根据用户ID查询管理员信息
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUserId, user.getId());
        return getOne(queryWrapper);
    }

    /**
     * 检查是否为书籍审核管理员
     * @param username 用户名
     * @return 是否为书籍审核管理员
     */
    @Override
    public boolean isReportAdmin(String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 检查用户权限是否包含REPORT_ADMIN角色
        return user.getRoles() != null && user.getRoles().contains("REPORT_ADMIN");
    }

    /**
     * 检查是否为学生管理员
     * @param username 用户名
     * @return 是否为学生管理员
     */
    @Override
    public boolean isStudentAdmin(String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 检查用户权限是否包含STUDENT_ADMIN角色
        return user.getRoles() != null && user.getRoles().contains("STUDENT_ADMIN");
    }
} 