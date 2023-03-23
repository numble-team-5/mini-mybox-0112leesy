package com.numble.mybox.service.impl;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.numble.mybox.common.CommonResponse;
import com.numble.mybox.config.security.JwtTokenProvider;
import com.numble.mybox.data.dto.SignInResultDto;
import com.numble.mybox.data.dto.SignUpResultDto;
import com.numble.mybox.data.entity.User;
import com.numble.mybox.data.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({SignServiceImpl.class})
public class SignServiceTest {

    @MockBean
    UserRepository userRepository;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    SignServiceImpl signService;

    final String USERNAME = "test-username";
    final String PASSWORD = "test-password";
    final String PASSWORD_ENCODE = "test-password-encode";
    final String TOKEN = "test-token";
    final String EMAIL = "test-email";
    final List<String> ROLE = Collections.singletonList("ROLE_USER");

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccessTest() {
        // given
        Mockito.when(passwordEncoder.encode(PASSWORD))
            .thenReturn(PASSWORD_ENCODE);
        Mockito.when(userRepository.save(any(User.class)))
            .then(returnsFirstArg());

        // when
        SignUpResultDto signUpResultDto = signService.signUp(
            USERNAME, PASSWORD, EMAIL, "user"
        );

        // then
        Assertions.assertEquals(signUpResultDto.getCode(), CommonResponse.SUCCESS.getCode());
        Assertions.assertEquals(signUpResultDto.getMsg(), CommonResponse.SUCCESS.getMsg());

        verify(userRepository).save(any());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    @DisplayName("회원가입 실패 테스트")
    void signUpFailTest() {
        // given
        Mockito.when(passwordEncoder.encode(PASSWORD))
            .thenReturn(PASSWORD_ENCODE);
        Mockito.when(userRepository.save(any(User.class)))
            .then(returnsFirstArg());

        // when
        SignUpResultDto signUpResultDto = signService.signUp(
            "", PASSWORD, EMAIL, "user"
        );

        // then
        Assertions.assertEquals(signUpResultDto.getCode(), CommonResponse.FAIL.getCode());
        Assertions.assertEquals(signUpResultDto.getMsg(), CommonResponse.FAIL.getMsg());

        verify(userRepository).save(any());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void signInSuccessTest() {
        // given
        Mockito.when(passwordEncoder.matches(PASSWORD, PASSWORD_ENCODE))
            .thenReturn(true);
        Mockito.when(userRepository.getByUsername(USERNAME))
            .thenReturn(new User(1L, USERNAME, PASSWORD_ENCODE, EMAIL, ROLE));
        Mockito.when(jwtTokenProvider.createToken(String.valueOf(USERNAME), ROLE)).thenReturn(TOKEN);

        // when
        SignInResultDto signInResultDto = signService.signIn(USERNAME, PASSWORD);

        // then
        Assertions.assertEquals(signInResultDto.getCode(), CommonResponse.SUCCESS.getCode());
        Assertions.assertEquals(signInResultDto.getMsg(), CommonResponse.SUCCESS.getMsg());
        Assertions.assertEquals(signInResultDto.getToken(), TOKEN);

        verify(passwordEncoder).matches(PASSWORD, PASSWORD_ENCODE);
        verify(userRepository).getByUsername(USERNAME);
        verify(jwtTokenProvider).createToken(String.valueOf(USERNAME), ROLE);
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void signInFailTest() {
        // given
        Mockito.when(passwordEncoder.matches(PASSWORD, PASSWORD_ENCODE))
            .thenReturn(false);
        Mockito.when(userRepository.getByUsername(USERNAME))
            .thenReturn(new User(1L, USERNAME, PASSWORD_ENCODE, EMAIL, ROLE));

        // when
        Assertions.assertThrows(RuntimeException.class, () -> {
            signService.signIn(USERNAME, PASSWORD);
        });

        // then
        verify(passwordEncoder).matches(PASSWORD, PASSWORD_ENCODE);
        verify(userRepository).getByUsername(USERNAME);
    }

}
