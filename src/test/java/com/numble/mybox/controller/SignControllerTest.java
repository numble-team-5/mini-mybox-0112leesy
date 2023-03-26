package com.numble.mybox.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.numble.mybox.data.dto.SignInResultDto;
import com.numble.mybox.data.dto.SignUpResultDto;
import com.numble.mybox.service.impl.SignServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriHost = "api", uriPort = 8081)
@Transactional
public class SignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    SignServiceImpl signService;

    @Test
    @DisplayName("로그인 테스트")
    void signInTest() throws Exception {
        // given
        String username = "test-username";
        String password = "test-password";
        given(signService.signIn(username, password))
            .willReturn(new SignInResultDto(true, 0, "Success", "test-token"));

        // when
        ResultActions actions = mockMvc.perform(
            post(String.format("/sign-api/sign-in?username=%s&password=%s", username, password)));

        // then
        actions.andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
            .andDo(print());

        verify(signService).signIn(username, password);

        // docs
        actions.andDo(document("sign-in-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("username").description("아이디"),
                parameterWithName("password").description("비밀번호")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공여부"),
                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                fieldWithPath("msg").type(JsonFieldType.STRING).description("메시지"),
                fieldWithPath("token").type(JsonFieldType.STRING).description("발급토큰")
            )
        ));
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() throws Exception {
        // given
        String username = "test-username";
        String password = "test-password";
        String email = "test-email";
        String role = "user";
        given(signService.signUp(username, password, email, role))
            .willReturn(new SignUpResultDto(true, 0, "Success"));

        // when
        ResultActions actions = mockMvc.perform(
            post(String.format("/sign-api/sign-up?username=%s&password=%s&email=%s&role=%s",
                username, password, email, role)));

        // then
        actions.andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").exists())
            .andDo(print());

        verify(signService).signUp(username, password, email, role);

        // docs
        actions.andDo(document("sign-up-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("username").description("아이디"),
                parameterWithName("password").description("비밀번호"),
                parameterWithName("email").description("이메일"),
                parameterWithName("role").description("권한")
            ),
            responseFields(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공여부"),
                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                fieldWithPath("msg").type(JsonFieldType.STRING).description("메시지")
            )
        ));
    }

}
