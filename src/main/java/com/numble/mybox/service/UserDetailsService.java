package com.numble.mybox.service;

import com.numble.mybox.data.entity.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

//    UserResponseDto getUser(Long number);
//
//    UserResponseDto saveUser(UserDto userDto);
//    UserResponseDto loadUserByUsername(String username) throws UsernameNotFoundException;
//
//    void deleteUser(Long number) throws Exception;
}
