package com.numble.mybox.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.service.impl.BucketServiceImpl;
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

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriHost = "api", uriPort = 8081)
class BucketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    BucketServiceImpl bucketService;

    @Test
    @DisplayName("버킷 지정 테스트")
    void assignNewBucket() throws Exception {
        // given
        given(bucketService.createBucket()).willReturn("newBucketName");
        given(bucketService.assignBucket("test-user", "newBucketName")).willReturn(
            new BucketResponseDto("test-user", "newBucketName", 30.0));

        // when
        ResultActions actions = mockMvc.perform(
            post(String.format("/bucket-api/bucket?username=" + "test-user")));

        // then
        actions.andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bucketName").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.remain").exists())
            .andDo(print());

        verify(bucketService).createBucket();
        verify(bucketService).assignBucket("test-user", "newBucketName");

        // docs
        actions.andDo(document("assign-bucket-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("username").description("아이디")
            ),
            responseFields(
                fieldWithPath("username").type(JsonFieldType.STRING).description("아이디"),
                fieldWithPath("bucketName").type(JsonFieldType.STRING).description("버킷이름"),
                fieldWithPath("remain").type(JsonFieldType.NUMBER).description("남은용량")
            )
        ));
    }
}