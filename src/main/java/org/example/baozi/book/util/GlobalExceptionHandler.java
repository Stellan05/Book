package org.example.baozi.book.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.response.ResponseMessage;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * JWT 异常捕获
     * @param e
     * @return
     */
    @ExceptionHandler(value = JwtException.class)
    @ResponseBody
    public ResponseMessage<String> handleJwtException(JwtException e) {
        log.error(e.getMessage(), e);
        return ResponseMessage.error("token验证错误");
    }


    @ExceptionHandler(value = ExpiredJwtException.class)
    @ResponseBody
    public ResponseMessage<String> handleExpiredJwtException(ExpiredJwtException e) {
        log.error(e.getMessage(), e);
        return ResponseMessage.error("token已过期");
    }

    /**
     * 参数效验错误
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseMessage<String> handleConstraintViolationException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return ResponseMessage.error("参数不符合规范");
    }
}
