package com.numble.mybox.service;

import com.numble.mybox.data.dto.UserDto;
import com.numble.mybox.data.dto.UserResponseDto;

public interface UserService {

    UserResponseDto getUser(Long number);

    UserResponseDto saveUser(UserDto userDto);

    void deleteUser(Long number) throws Exception;
}
