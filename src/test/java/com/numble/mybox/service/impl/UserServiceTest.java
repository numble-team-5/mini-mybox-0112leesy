//package com.numble.mybox.service.impl;
//
//import static org.mockito.AdditionalAnswers.returnsFirstArg;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//
//import com.numble.mybox.data.dto.UserDto;
//import com.numble.mybox.data.dto.UserResponseDto;
//import com.numble.mybox.data.entity.User;
//import com.numble.mybox.data.repository.UserRepository;
//import com.numble.mybox.service.UserDetailsService;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//@ExtendWith(SpringExtension.class)
//@Import({UserDetailsServiceImpl.class})
//public class UserServiceTest {
//
//    @MockBean
//    UserRepository userRepository;
//    @MockBean
//    PasswordEncoder passwordEncoder;
//
//    @Autowired
//    UserDetailsService userDetailsService;
//
//    @Test
//    @DisplayName("User 저장 테스트")
//    void saveUserTest() {
//        // given
//        Mockito.when(userRepository.save(any(User.class)))
//            .then(returnsFirstArg());
//        Mockito.when(passwordEncoder.encode("pass1"))
//            .thenReturn("pass1-encode");
//
//        // when
//        UserResponseDto userResponseDto = userDetailsService.saveUser(
//            new UserDto("user1", "pass1", "email1"));
//
//        // then
//        Assertions.assertEquals(userResponseDto.getUsername(), "user1");
//        Assertions.assertEquals(userResponseDto.getPassword(), "pass1-encode");
//        Assertions.assertEquals(userResponseDto.getEmail(), "email1");
//
//        verify(userRepository).save(any());
//        verify(passwordEncoder).encode("pass1");
//    }
//
//}
