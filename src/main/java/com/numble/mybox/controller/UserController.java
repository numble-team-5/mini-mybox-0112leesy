package com.numble.mybox.controller;

import com.numble.mybox.data.dto.UserDto;
import com.numble.mybox.data.dto.UserResponseDto;
import com.numble.mybox.service.StorageService;
import com.numble.mybox.service.UserService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final StorageService storageService;

    @Autowired
    public UserController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    // 회원 정보 저장 및 버킷 생성
    @Transactional
    @PostMapping()
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserDto userDto) {
        UserResponseDto userResponseDto = userService.saveUser(userDto);
        storageService.createBucket(userResponseDto.getBucket());
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }

}
