package org.example.baozi.book.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.baozi.book.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义UserDetails实现
 * 封装了用户信息和权限
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    
    private User user;
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Integer getUserId() {
        return user.getId();
    }
    
    /**
     * 获取用户权限
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取密码
     * @return 密码
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    /**
     * 获取用户名
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    /**
     * 账户是否未过期
     * @return 是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * 账户是否未锁定
     * @return 是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * 凭证是否未过期
     * @return 是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * 账户是否启用
     * @return 是否启用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}