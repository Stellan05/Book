package org.example.baozi.book.util;

/**
 * 密码工具类
 * 用于处理密码生成和校验
 */
public class PasswordUtil {
    
    /**
     * 生成学生初始密码
     * 规则为"zjut+学号后六位"
     * @param studentId 学号
     * @return 初始密码
     */

    public static String generateInitialPassword(String studentId) {
        if (studentId == null || studentId.length() < 6) {
            throw new IllegalArgumentException("studentId must contain at least 6 characters");
        }
        
        // 提取学号后6位
        String lastSixDigits = studentId.substring(studentId.length() - 6);
        
        return "zjut" + lastSixDigits;
    }
} 