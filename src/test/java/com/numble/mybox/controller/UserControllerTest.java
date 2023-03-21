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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.google.gson.Gson;
import com.numble.mybox.data.dto.UserDto;
import com.numble.mybox.data.dto.UserResponseDto;
import com.numble.mybox.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriHost = "api", uriPort = 8081)
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserServiceImpl userService;

    @Test
    @DisplayName("User 생성 테스트")
    void createUserTest() throws Exception {
        // given
        given(userService.saveUser(new UserDto("user", "pass", "email")))
            .willReturn(new UserResponseDto(123L, "user", "pass", "email", "bucket", 30.0));

        UserDto userDto = UserDto.builder()
            .username("user")
            .password("pass")
            .email("email")
            .build();

        Gson gson = new Gson();
        String content = gson.toJson(userDto);

        // when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/user").contentType(
            MediaType.APPLICATION_JSON).content(content));

        // then
        actions.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.password").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bucket").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.remain").exists())
            .andDo(print());

        verify(userService).saveUser(new UserDto("user", "pass", "email"));

        // docs
        actions.andDo(document("user-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일")
            ),
            responseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("인덱스"),
                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("bucket").type(JsonFieldType.STRING).description("버킷이름"),
                fieldWithPath("remain").type(JsonFieldType.NUMBER).description("남은용량")
            )
        ));

    }

}
