package org.example.baozi.book.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.security.UserDetailsImpl;
import org.example.baozi.book.security.UserDetailsServiceImpl;
import java.util.concurrent.TimeUnit;
import org.example.baozi.book.service.TokenService;
import org.example.baozi.book.service.impl.RedisServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 一个 JWT的 token过滤器，其功能与拦截器相差不大，即验证 token，提取用户信息
 * 但是因为 Security中使用过滤器链而不是拦截器链，直接在 Security 过滤器链中加入 JWT 解析，可以无缝集成 Spring Security 的授权机制。
 * 且过滤器（Filter）比拦截器（Interceptor）更早执行
 * 过滤器可以在 Security 认证链 最早阶段 解析 Token，确保后续认证链都能获取到正确的用户身份。
 * 之后存入 SecurityContextHolder，类似拦截器中的存储信息在本地线程的操作，同样基于 ThreadLocal
 * 支持黑名单校验、token刷新等内容
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;
    private final RedisServiceImpl redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            // 如果请求头中没有Authorization信息，直接放行
            filterChain.doFilter(request, response);
            return;
        }
        
        // 提取Token
        String token = authHeader.substring(7);
        
        try {
            // 验证Token，包括有效性和黑名单检查
            if (!tokenService.validateToken(token)) {
                log.warn("Token验证失败：{}", token);
                filterChain.doFilter(request, response);
                return;
            }
            
            // 获取用户名
            String username = tokenService.getUsernameFromToken(token);
            if (username == null) {
                log.error("无法从Token中获取用户名");
                filterChain.doFilter(request, response);
                return;
            }

            // 如果Token验证通过且用户未被禁用
            // 先从Redis中获取用户信息，提高性能
//            String userKey = RedisServiceImpl.KEY_PREFIX_USER + username;
//            Map claims = (Map) redisService.getValue(userKey);
//            UserDetailsImpl userDetails = userDetailsService.loadUserByUsername((String) claims.get("username"));
//
//            // 如果Redis中没有，则从数据库加载
//            if (userDetails == null) {
//                log.debug("Redis中没有用户信息，从数据库加载：{}", username);
//                userDetails = userDetailsService.loadUserByUsername(username);
//
//                // 将用户信息保存到Redis，提高后续访问性能
//                redisService.setValueWithExpire(userKey, userDetails, 24 * 60 * 60, TimeUnit.SECONDS);
//            }
//

            UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);

            // 创建认证对象
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 设置认证信息到SecurityContext--Security的核心，之后的认证操作基于SecurityContextHolder来读取
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("Token认证成功：{}", username);
            
        } catch (Exception e) {
            log.error("Token认证过程中发生错误", e);
        }
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}
