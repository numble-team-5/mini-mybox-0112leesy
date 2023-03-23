package com.numble.mybox.service;

import com.numble.mybox.data.dto.SignInResultDto;
import com.numble.mybox.data.dto.SignUpResultDto;

public interface SignService {

    SignUpResultDto signUp(String username, String password, String email, String role);

    SignInResultDto signIn(String username, String password) throws RuntimeException;

}
