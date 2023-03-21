package com.numble.mybox.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.numble.mybox.data.dto.UserDto;
import com.numble.mybox.data.dto.UserResponseDto;
import com.numble.mybox.data.entity.User;
import com.numble.mybox.data.repository.UserRepository;
import com.numble.mybox.service.UserService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AmazonS3 amazonS3) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDto getUser(Long number) {
        return null;
    }

    @Override
    public UserResponseDto saveUser(UserDto userDto) {
        LOGGER.info("[saveUser] userDto : {}", userDto.toString());
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setBucket(UUID.randomUUID().toString());
        user.setRemain(30.0);

        User savedUser = userRepository.save(user);
        LOGGER.info("[savedUser] savedUser : {}", savedUser.toString());

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(savedUser.getId());
        userResponseDto.setUsername(savedUser.getUsername());
        userResponseDto.setPassword(savedUser.getPassword());
        userResponseDto.setEmail(savedUser.getEmail());
        userResponseDto.setBucket(savedUser.getBucket());
        userResponseDto.setRemain(savedUser.getRemain());

        return userResponseDto;
    }

    @Override
    public void deleteUser(Long number) throws Exception {

    }

}
