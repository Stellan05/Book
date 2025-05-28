package org.example.baozi.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.dto.StuInfo;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.entity.User;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.StudentService;
import org.example.baozi.book.service.UserService;
import org.example.baozi.book.util.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 首次登录修改个人信息及其密码
     * @param request 浏览器请求
     * @param stuInfo 修改信息的封装DTO类
     * @return 修改结果
     */
    @PostMapping("/update-info")
    public ResponseMessage<String> changePasswordOnFirstLogin(
            HttpServletRequest request,
            @RequestBody @Valid StuInfo stuInfo) {

        String token = JWTUtil.getTokenFromAuthorization(request);
        String studentId = JWTUtil.getUsernameFromToken(token);

        User user = userService.getUserByUsername(studentId);
        if(user == null) {
            return ResponseMessage.error("用户信息不存在");
        }
        // 获取学生信息
        Student student = studentService.getStudentById(studentId);
        if (student == null) {
            return ResponseMessage.error("学生信息不存在");
        }

        String newPassword = stuInfo.getNewPassword();
        String confirmPassword = stuInfo.getConfirmPassword();
        String campus = stuInfo.getCampus();
        String phoneNumber = stuInfo.getPhone();
        String dormitory = stuInfo.getDormitory();
        String paymentMethod = stuInfo.getPayment_method();


        // 检查是否首次登录
        if (student.getFirstLogin()) {
            // 更新首次登录状态
            student.setFirstLogin(false);
        }
        if(newPassword.equals(confirmPassword)) {
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateById(user);
        }
        else{
            return ResponseMessage.error("确认密码与新密码不同");
        }

        student.setPhone(phoneNumber);
        student.setCampus(campus);
        student.setDormitory(dormitory);
        student.setPaymentMethod(paymentMethod);
        studentService.updateById(student);

        return ResponseMessage.success("学生信息更新成功");
    }


}
