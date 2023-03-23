package com.numble.mybox.service.impl;

import com.numble.mybox.data.dto.UserDto;
import com.numble.mybox.data.dto.UserResponseDto;
import com.numble.mybox.data.entity.User;
import com.numble.mybox.data.entity.UserDetails;
import com.numble.mybox.data.repository.UserRepository;
import com.numble.mybox.service.UserDetailsService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("[loadUserByUsername] username : {}", username);
        return userRepository.getByUsername(username);
    }

//    @Override
//    public UserResponseDto getUser(Long number) {
//        return null;
//    }
//
//    @Override
//    public UserResponseDto saveUser(UserDto userDto) {
//        LOGGER.info("[saveUser] userDto : {}", userDto.toString());
//        User user = new User();
//        user.setUsername(userDto.getUsername());
//        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
//        user.setEmail(userDto.getEmail());
//        user.setBucket(UUID.randomUUID().toString());
//        user.setRemain(30.0);
//
//        User savedUser = userRepository.save(user);
//        LOGGER.info("[savedUser] savedUser : {}", savedUser.toString());
//
//        UserResponseDto userResponseDto = new UserResponseDto();
//        userResponseDto.setId(savedUser.getId());
//        userResponseDto.setUsername(savedUser.getUsername());
//        userResponseDto.setPassword(savedUser.getPassword());
//        userResponseDto.setEmail(savedUser.getEmail());
//        userResponseDto.setBucket(savedUser.getBucket());
//        userResponseDto.setRemain(savedUser.getRemain());
//
//        return userResponseDto;
//    }
//
//    @Override
//    public UserResponseDto loadUserByUsername(String username) {
//        LOGGER.info("[loadUserByUsername] username : {}", username);
//        User user = userRepository.getByUsername(username);
//
//        UserResponseDto userResponseDto = new UserResponseDto();
//        userResponseDto.setId(user.getId());
//        userResponseDto.setUsername(user.getUsername());
//        userResponseDto.setPassword(user.getPassword());
//        userResponseDto.setEmail(user.getEmail());
//        userResponseDto.setBucket(user.getBucket());
//        userResponseDto.setRemain(user.getRemain());
//
//        return userResponseDto;
//    }
//
//    @Override
//    public void deleteUser(Long number) throws Exception {
//
//    }

}
