package org.example.baozi.book.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.Admin;
import org.example.baozi.book.entity.Collector;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.AdminService;
import org.example.baozi.book.service.CollectorService;
import org.example.baozi.book.service.StudentService;
import org.example.baozi.book.service.UserService;
import org.example.baozi.book.service.impl.AuthServiceImpl;
import org.example.baozi.book.util.PasswordUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final StudentService studentService;

    private final AdminService adminService;

    private final CollectorService collectorService;

    private final PasswordEncoder passwordEncoder;

    private final AuthServiceImpl authService;


    /**
     * 刷新令牌接口
     * @param request HTTP请求
     * @return 新的令牌
     */
    @PostMapping("/refresh-token")
    public ResponseMessage<Map<String, String>> refreshToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            return ResponseMessage.error("无效的Token格式");
        }
        
        String oldToken = authorization.substring(7);
        String newToken = authService.refreshToken(oldToken);
        
        if (newToken == null) {
            return ResponseMessage.error("Token刷新失败，请重新登录");
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("token", newToken);
        return ResponseMessage.success(response);
    }
    
    /**
     * 退出登录接口
     * @param request HTTP请求
     * @return 退出结果
     */
    @PostMapping("/logout")
    public ResponseMessage<String> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            return ResponseMessage.error("无效的Token格式");
        }
        
        String token = authorization.substring(7);
        boolean success = authService.logout(token);
        
        if (success) {
            return ResponseMessage.success("退出登录成功");
        } else {
            return ResponseMessage.error("退出登录失败");
        }
    }


    /**
     * 学生注册接口
     *
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register/student")
    public ResponseMessage<String> studentRegister(@RequestBody Map<String, String> registerRequest) {
        String studentId = registerRequest.get("studentId");
        String password = registerRequest.get("password");

        // 检查学号和密码是否为空
        if (studentId == null || studentId.isEmpty() || password == null || password.isEmpty()) {
            return ResponseMessage.error("学号和密码不能为空");
        }

        // 检查该学号是否已经注册
        User existingUser = userService.getUserByUsername(studentId);
        if (existingUser != null) {
            return ResponseMessage.error("该学号已经注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(studentId);
        // 加密密码
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("STUDENT");
        userService.save(user);

        // 创建学生信息
        studentService.createStudent(user.getId(), studentId);

        return ResponseMessage.success("注册成功");
    }

    /**
     * 学生登录接口
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login/student")
    public ResponseMessage<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String studentId = loginRequest.get("studentId");
        String password = loginRequest.get("password");

        // 检查学号和密码格式
        if (StringUtils.isEmpty(studentId) || StringUtils.isEmpty(password) || StringUtils.isBlank(password) || StringUtils.isBlank(studentId) || studentId.length() != 12) {
            return ResponseMessage.error("学号或密码格式错误");
        }

        // 获取用户信息
        User user = userService.getUserByUsername(studentId);
        if (user == null) {
            return ResponseMessage.error("用户不存在");
        }

        // 获取学生信息
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseMessage.error("学生信息不存在");
        }

        Map<String, Object> response = new HashMap<>();

        try {
            // 如果首次登录
            if (student.getFirstLogin()) {
                // 初始密码
                String initialPassword = PasswordUtil.generateInitialPassword(studentId);
                if (passwordEncoder.matches(password, user.getPassword())) {
                    response.put("firstLogin", true);
                    response.put("message", "首次登录，请修改密码");
                    response.put("initialPassword", initialPassword);

                    // 通过认证服务生成JWT令牌
                    String token = authService.login(studentId, password);
                    response.put("token", token);

                    return ResponseMessage.success(response);
                } else {
                    return ResponseMessage.error("密码错误");
                }
            } else {
                // 非首次登录，直接使用认证服务进行认证并获取令牌
                String token = authService.login(studentId, password);

                response.put("firstLogin", false);
                response.put("message", "登录成功，下发token");
                response.put("token", token);

                return ResponseMessage.success(response);
            }
        } catch (Exception e) {
            // 认证失败
            return ResponseMessage.error("用户名或密码错误");
        }
    }
    /**
     * 管理员登录接口
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login/admin")
    public ResponseMessage<Map<String, Object>> adminLogin(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // 检查用户名和密码格式
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isBlank(password) || StringUtils.isBlank(username) ) {
            return ResponseMessage.error("用户名或密码格式错误");
        }

        Map<String, Object> response = new HashMap<>();

        try {
                String token = authService.login(username, password);
                response.put("message", "登录成功，下发token");
                response.put("token", token);
                return ResponseMessage.success(response);
        } catch (Exception e) {
            // 认证失败
            return ResponseMessage.error("用户名或密码错误");
        }
    }

    /**
     * 收书员登录接口
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login/collector")
    public ResponseMessage<Map<String, Object>> collectorLogin(@RequestBody Map<String, String> loginRequest) {
        String collectorId = loginRequest.get("collectorId");
        String password = loginRequest.get("password");

        // 检查学号和密码格式
        if (StringUtils.isEmpty(collectorId) || StringUtils.isEmpty(password) || StringUtils.isBlank(password) || StringUtils.isBlank(collectorId)) {
            return ResponseMessage.error("学号或密码格式错误");
        }

        Map<String, Object> response = new HashMap<>();

        try {
            String token = authService.login(collectorId, password);
            response.put("message", "登陆成功，下发token");
            response.put("token", token);
            return ResponseMessage.success(response);
        } catch (Exception e) {
            // 认证失败
            return ResponseMessage.error("用户名或密码错误");
        }
    }
    /**
     * 收书员注册
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register/collector")
    public ResponseMessage<?> collectorRegister(@RequestBody Map<String, String> registerRequest) {
        String collectorId = registerRequest.get("collectorId");
        String password = registerRequest.get("password");
        String realName = registerRequest.get("realName");
        String phone = registerRequest.get("phone");
        String campus = registerRequest.get("campus");
        String payment = registerRequest.get("paymentMethod");

        // 检查参数
        if (collectorId == null || password == null || realName == null || phone == null || campus == null) {
            return ResponseMessage.error("参数错误，请完整填写所有信息");
        }

        // 检查学号是否已存在
        User existingUser = userService.getUserByUsername(collectorId);
        if (existingUser != null) {
            return ResponseMessage.error("该学号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(collectorId);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("COLLECTOR");
        userService.save(user);

        // 创建收书员
        Collector collector = collectorService.registerCollector(user.getId(), collectorId, realName, phone, campus,payment);

        // 生成token
        String token = authService.login(collectorId, password);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册成功");
        response.put("token", token);
        response.put("collector", collector);

        return ResponseMessage.success(response);
    }
}
