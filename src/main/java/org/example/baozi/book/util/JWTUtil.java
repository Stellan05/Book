package org.example.baozi.book.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT token 令牌工具类配置
 */
@Component
@Slf4j
public class JWTUtil {
    // 使用 HS256 密钥时，密钥长度需要满足大于32字节，以防被暴力破解
    /*
      *private final static String secretKey = "my-secret-key-my-secret-ket-plus-32-characters";
      *private final static SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
      这是固定密钥写法
     */
     private final static String secretKey = "my-secret-key-my-secret-ket-plus-32-characters";
     private final static SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    // 由系统随机生成一个强随机密钥
    //  private final static SecretKey SIGNING_KEY=Jwts.SIG.HS256.key().build();
    // 过期时间 24小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成JWT令牌
     * @param claims
     * @return
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .signWith(SIGNING_KEY,Jwts.SIG.HS256)// 指定签名算法和密钥
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token jwt令牌
     * @return JWT第二部分负载payload储存的内容 --- 即Claims是 payload
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /**
     * 效验token的有效性
     * @param token Token
     * @return 返回是否有效
     */
    public static boolean validateToken(String token) {
        try{
            parseToken(token);
            return true;
        }catch(JwtException | IllegalArgumentException e ){
            log.error("验证失败{}",e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取用户id
     * @param token
     * @return
     */
    public static Integer getIdFromToken(String token) {
        try{
            Claims claims = parseToken(token);
            return claims.get("userId", Integer.class);
        }catch(JwtException | IllegalArgumentException e ){
            log.error("解析用户id失败{}", e.getMessage());
            return -1;
        }
    }
    /**
     * 从token里解析用户名
     * @param token
     * @return
     */
    public static String getUsernameFromToken(String token) {
        try{
            Claims claims = parseToken(token);
            return claims.get("username", String.class);
        }catch(JwtException | IllegalArgumentException e ){
            log.error("解析用户名失败{}", e.getMessage());
            return null;
        }
    }

    /**
     * token里解析用户权限
     * @param token
     * @return
     */
    public static String getRoleFromToken(String token) {
        try{
            Claims claims = parseToken(token);
            return claims.get("roles", String.class);
        }catch(JwtException | IllegalArgumentException e ){
            log.error("token获取用户身份失败{}", e.getMessage());
            return null;
        }
    }
    public static String refreshToken(String oldToken) {
        Claims claims = parseToken(oldToken);
        if (claims == null) {
            return null; // 如果 Token 无效，返回 null
        }

        // 获取原始的用户信息（比如 userId）
        Integer userId = claims.get("userId", Integer.class);
        if (userId == null) {
            return null; // 如果没有用户信息，返回 null
        }

        // 重新生成新的 Token，设置新的过期时间等
        return Jwts.builder()
                .claims(claims)
                .signWith(SIGNING_KEY,Jwts.SIG.HS256)
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .compact(); // 拼接
    }

    /**
     * 从浏览器请求中提取token
     * @param request 浏览器请求
     * @return token
     */
    public static String getTokenFromAuthorization(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("Missing or invalid token");
            throw new RuntimeException("Missing or invalid token");
        }
        String token = authorization.substring(7); // 去掉 Bearer
        if (!JWTUtil.validateToken(token)) {
            log.error("token效验失败");
            throw new RuntimeException("token效验失败");
        }
        return token;
    }
}

