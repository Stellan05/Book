package org.example.baozi.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.ReasonTemplate;
import org.example.baozi.book.entity.Report;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.AdminService;
import org.example.baozi.book.service.ReasonTemplateService;
import org.example.baozi.book.service.ReportService;
import org.example.baozi.book.service.UserService;
import org.example.baozi.book.util.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员相关请求
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ReportService reportService;
    private final ReasonTemplateService reasonTemplateService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 管理员修改密码
     * @param request 请求
     * @param passwordMap 密码信息
     * @return 响应消息
     */
    @PostMapping("/password")
    public ResponseMessage<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> passwordMap) {
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为管理员
        if (!adminService.isReportAdmin(username) && !adminService.isStudentAdmin(username)) {
            return ResponseMessage.error("无权限操作");
        }
        
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseMessage.error("密码不能为空");
        }
        
        // 获取用户信息
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseMessage.error("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseMessage.error("旧密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        
        return ResponseMessage.success("密码修改成功");
    }
    
    /**
     * 获取举报信息列表
     * @param page 页码
     * @param size 每页大小
     * @param status 处理状态
     * @return 分页结果
     */
    @GetMapping("/reports")
    public ResponseMessage<PageResult<Report>> getReportList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status) {
        
        PageResult<Report> result = reportService.getReportPage(page, size, status);
        return ResponseMessage.success(result);
    }
    
    /**
     * 处理举报信息
     * @param request 请求
     * @param id 举报ID
     * @param handleMap 处理信息
     * @return 响应消息
     */
    @PostMapping("/reports/{id}/handle")
    public ResponseMessage<?> handleReport(
            HttpServletRequest request,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> handleMap) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        Integer adminId = JWTUtil.getIdFromToken(token);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为书籍审核管理员
        if (!adminService.isReportAdmin(username)) {
            return ResponseMessage.error("无权限处理举报信息");
        }
        
        Integer result = (Integer) handleMap.get("result");
        String opinion = (String) handleMap.get("opinion");
        Boolean deductCredit = (Boolean) handleMap.get("deductCredit");
        Integer diffScore = (Integer) handleMap.get("diffScore");
        
        if (result == null || opinion == null) {
            return ResponseMessage.error("处理结果和处理意见不能为空");
        }
        
        boolean success = reportService.handleReport(id, result, adminId, opinion, deductCredit != null && deductCredit,diffScore);
        
        if (success) {
            return ResponseMessage.success("处理成功");
        } else {
            return ResponseMessage.error("处理失败");
        }
    }
    
    /**
     * 批量处理举报信息
     * @param request 请求
     * @param handleMap 处理信息
     * @return 响应消息
     */
    @PostMapping("/reports/batch-handle")
    public ResponseMessage<?> batchHandleReport(
            HttpServletRequest request,
            @RequestBody Map<String, Object> handleMap) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为书籍审核管理员
        if (!adminService.isReportAdmin(username)) {
            return ResponseMessage.error("无权限处理举报信息");
        }
        
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) handleMap.get("ids");
        Integer result = (Integer) handleMap.get("result");
        String opinion = (String) handleMap.get("opinion");
        Boolean deductCredit = (Boolean) handleMap.get("deductCredit");
        
        if (ids == null || ids.isEmpty() || result == null || opinion == null) {
            return ResponseMessage.error("参数错误");
        }
        
        boolean success = reportService.batchHandleReport(ids, result, username, opinion, deductCredit != null && deductCredit);
        
        if (success) {
            return ResponseMessage.success("处理成功");
        } else {
            return ResponseMessage.error("处理失败");
        }
    }
    
    /**
     * 撤销举报处理
     * @param request 请求
     * @param id 举报ID
     * @return 响应消息
     */
    @PostMapping("/reports/{id}/cancel")
    public ResponseMessage<?> cancelReportHandle(
            HttpServletRequest request,
            @PathVariable Integer id) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        Integer adminId = JWTUtil.getIdFromToken(token);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为书籍审核管理员
        if (!adminService.isReportAdmin(username)) {
            return ResponseMessage.error("无权限撤销举报处理");
        }
        
        boolean success = reportService.cancelReport(id, adminId);
        
        if (success) {
            return ResponseMessage.success("撤销成功");
        } else {
            return ResponseMessage.error("撤销失败");
        }
    }
    
    /**
     * 获取举报详情
     * @param request 请求
     * @param id 举报ID
     * @return 响应消息
     */
    @GetMapping("/reports/{id}")
    public ResponseMessage<Report> getReportDetail(
            HttpServletRequest request,
            @PathVariable Integer id) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为书籍审核管理员
        if (!adminService.isReportAdmin(username)) {
            return ResponseMessage.error("无权限查看举报详情");
        }
        
        Report report = reportService.getReportDetail(id);
        
        if (report != null) {
            return ResponseMessage.success(report);
        } else {
            return ResponseMessage.error("举报信息不存在");
        }
    }
    
    /**
     * 获取理由模板列表
     * @param request 请求
     * @param type 理由类型
     * @return 响应消息
     */
    @GetMapping("/reasons")
    public ResponseMessage<List<ReasonTemplate>> getReasonTemplates(
            HttpServletRequest request,
            @RequestParam(value = "type", required = false) Integer type) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为管理员
        if (!adminService.isReportAdmin(username) && !adminService.isStudentAdmin(username)) {
            return ResponseMessage.error("无权限查看理由模板");
        }
        
        List<ReasonTemplate> templates = reasonTemplateService.getTemplatesByType(type, username);
        return ResponseMessage.success(templates);
    }
    
    /**
     * 添加理由模板
     * @param request 请求
     * @param templateMap 模板信息
     * @return 响应消息
     */
    @PostMapping("/reasons")
    public ResponseMessage<?> addReasonTemplate(
            HttpServletRequest request,
            @RequestBody Map<String, Object> templateMap) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为管理员
        if (!adminService.isReportAdmin(username) && !adminService.isStudentAdmin(username)) {
            return ResponseMessage.error("无权限添加理由模板");
        }
        
        String content = (String) templateMap.get("content");
        Integer type = (Integer) templateMap.get("type");
        
        if (content == null || type == null) {
            return ResponseMessage.error("理由内容和类型不能为空");
        }
        
        boolean success = reasonTemplateService.addTemplate(username, content, type);
        
        if (success) {
            return ResponseMessage.success("添加成功");
        } else {
            return ResponseMessage.error("添加失败");
        }
    }
    
    /**
     * 更新理由模板
     * @param request 请求
     * @param id 模板ID
     * @param templateMap 模板信息
     * @return 响应消息
     */
    @PutMapping("/reasons/{id}")
    public ResponseMessage<?> updateReasonTemplate(
            HttpServletRequest request,
            @PathVariable Integer id,
            @RequestBody Map<String, String> templateMap) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为管理员
        if (!adminService.isReportAdmin(username) && !adminService.isStudentAdmin(username)) {
            return ResponseMessage.error("无权限更新理由模板");
        }
        
        String content = templateMap.get("content");
        
        if (content == null) {
            return ResponseMessage.error("理由内容不能为空");
        }
        
        boolean success = reasonTemplateService.updateTemplate(id, content);
        
        if (success) {
            return ResponseMessage.success("更新成功");
        } else {
            return ResponseMessage.error("更新失败或模板不存在");
        }
    }
    
    /**
     * 删除理由模板
     * @param request 请求
     * @param id 模板ID
     * @return 响应消息
     */
    @DeleteMapping("/reasons/{id}")
    public ResponseMessage<?> deleteReasonTemplate(
            HttpServletRequest request,
            @PathVariable Integer id) {
        
        String token = JWTUtil.getTokenFromAuthorization(request);
        String username = JWTUtil.getUsernameFromToken(token);
        
        // 验证是否为管理员
        if (!adminService.isReportAdmin(username) && !adminService.isStudentAdmin(username)) {
            return ResponseMessage.error("无权限删除理由模板");
        }
        
        boolean success = reasonTemplateService.deleteTemplate(id);
        
        if (success) {
            return ResponseMessage.success("删除成功");
        } else {
            return ResponseMessage.error("删除失败或模板不存在");
        }
    }
} 