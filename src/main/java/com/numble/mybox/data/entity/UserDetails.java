package com.numble.mybox.data.entity;

import java.io.Serializable;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public interface UserDetails extends Serializable {

    // 계정이 가진 권한 목록 리턴
    Collection<? extends GrantedAuthority> getAuthorities();

    // 계정의 비밀번호 리턴
    String getPassword();

    // 계정의 아이디 리턴
    String getUsername();

    // 계정이 만료됐는지 리턴
    boolean isAccountNonExpired();

    // 계정이 잠겨있는지 리턴
    boolean isAccountNonLocked();

    // 비밀번호가 만료됐는지 리턴
    boolean isCredentialsNonExpired();

    // 계정이 활성화돼 있는지 리턴
    boolean isEnabled();

}
