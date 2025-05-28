package org.example.baozi.book.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.entity.Report;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.ReportService;
import org.example.baozi.book.util.JWTUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 举报相关控制器
 */
@RestController
@RequestMapping("/report")
@Slf4j
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/submit")
    public ResponseMessage<?> submitReport(
            HttpServletRequest request,
            @RequestBody Map<String, Object> report

    ){
        String token = JWTUtil.getTokenFromAuthorization(request);
        String studentId = JWTUtil.getUsernameFromToken(token);
        Long bookId = (Long)report.get("bookId");
        String reportedId = (String)report.get("reportedId");
        String reason = (String)report.get("reason");
        Integer type = (Integer)report.get("type");
        boolean result = reportService.submitReport(studentId,bookId,reportedId,reason,type);
        if(result){
            return ResponseMessage.success("举报提交成功");
        }
        else return ResponseMessage.error("举报提交失败，请稍后重试");
    }
}
