package org.example.baozi.book.config;

import lombok.RequiredArgsConstructor;
import org.example.baozi.book.filter.JwtAuthenticationTokenFilter;
import org.example.baozi.book.security.UserDetailsServiceImpl;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 密码编码器
     *
     * @return BCryptPasswordEncoder对象
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
//    /**
//     * 认证管理器(复杂版）
//     * @param http HttpSecurity对象
//     * @return AuthenticationManager对象
//     * @throws Exception 配置异常
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(passwordEncoder())
//                .and()
//                .build();
//    }

    /**
     *
     * @param authenticationConfig 包含了很多关于认证过程的配置
     * @return AuthenticationManager对象
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfig) throws Exception {
        return authenticationConfig.getAuthenticationManager();
    }
    /**
     * 安全过滤器链配置
     * @param http HttpSecurity对象
     * @return SecurityFilterChain对象
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 配置安全策略
        http
            // 禁用CSRF保护，方便前后端分离应用
            .csrf().disable()
            // 配置请求授权规则
            .authorizeHttpRequests()
                // 登录注册认证相关接口允许所有人访问
                .requestMatchers("/api/auth/register/**","/api/auth/login/**").permitAll()
                // 静态资源允许所有人访问
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // 学生角色才能访问的路径
                .requestMatchers("/student/**").hasRole("STUDENT")
                // 收书员角色才能访问的路径
               .requestMatchers("/collector/**").hasRole("COLLECTOR")
                // 管理人员的管理员可访问
                .requestMatchers("/admin/personnel/**").hasRole("PERSONNEL_ADMIN")
                // 管理社区规范的管理员可访问
                .requestMatchers("/admin/reports/**").hasRole("REPORT_ADMIN")
                // 其他请求需要认证
                .anyRequest().authenticated()

//            .and()
//            // 配置登录
//            .formLogin()
//                // 自定义登录页面
//                .loginPage("/login")
//                // 登录成功后跳转的页面
//                .defaultSuccessUrl("/index")
//                // 登录失败后跳转的页面
//                .failureUrl("/login?error=true")
//                // 自定义登录参数名
//                .usernameParameter("username")
//                .passwordParameter("password")
//                .permitAll()
//            .and()
//            // 配置登出
//            .logout()
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/login?logout=true")
//                .permitAll()
//            .and()
//            // 配置记住我功能
//            .rememberMe()
//                .key("bookRecycleRememberMeKey")
//                .tokenValiditySeconds(86400);// 记住登录状态一天
                .and()
                // 设置会话管理，因为使用JWT，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 添加JWT过滤器在UsernamePasswordAuthenticationFilter之前
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 