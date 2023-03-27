package com.numble.mybox.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.service.impl.ObjectServiceImpl;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriHost = "api", uriPort = 8081)
class ObjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    ObjectServiceImpl objectService;

    @Test
    @DisplayName("루트 오브젝트 조회 테스트")
    void getRootObject() throws Exception {
        // given
        String bucketName = "test-bucket";
        List<Object> rootObjects = new ArrayList<>();
        Object rootImageObject = Object.builder()
            .id(1L)
            .name("image1.jpg")
            .fullName("image1.jpg")
            .bucketName(bucketName)
            .size(2.3)
            .isFolder(false)
            .build();
        Object rootFolderObject = Object.builder()
            .id(2L)
            .name("folder1/")
            .fullName("folder1/")
            .bucketName(bucketName)
            .size(0.0)
            .isFolder(true)
            .build();
        rootObjects.add(rootImageObject);
        rootObjects.add(rootFolderObject);

        given(objectService.getRootObject(bucketName)).willReturn(rootObjects);

        // when
        ResultActions actions = mockMvc.perform(
            get("/object-api/bucket?bucketName="+bucketName)
        );

        // then
        String expectByBucketName = "$.data.[?(@.bucketName == '%s')]";
        String expectByName = "$.data.[?(@.name == '%s')]";
        String expectBySize = "$.data.[?(@.size == '%s')]";

        actions.andExpect(status().isOk())
            .andExpect(jsonPath(expectByBucketName, bucketName).exists())
            .andExpect(jsonPath("$.data[0].parentFullName").value(nullValue()))
            .andExpect(jsonPath(expectByName, "image1.jpg").exists())
            .andExpect(jsonPath(expectByName, "folder1/").exists())
            .andExpect(jsonPath(expectBySize, 2.3).exists())
            .andExpect(jsonPath(expectBySize, 0.0).exists())
            .andDo(print());

        verify(objectService).getRootObject(bucketName);

        // docs
        actions.andDo(document("get-root-object-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("bucketName").description("버킷이름")
            ),
            responseFields(
                fieldWithPath("data[0].id").type(JsonFieldType.NUMBER).description("인덱스"),
                fieldWithPath("data[0].name").type(JsonFieldType.STRING).description("파일/폴더 이름"),
                fieldWithPath("data[0].name").type(JsonFieldType.STRING).description("파일/폴더 이름"),
                fieldWithPath("data[0].fullName").type(JsonFieldType.STRING).description("전체경로"),
                fieldWithPath("data[0].parentFullName").type(JsonFieldType.NULL).description("상위 폴더 전체경로"),
                fieldWithPath("data[0].bucketName").type(JsonFieldType.STRING).description("버킷이름"),
                fieldWithPath("data[0].size").type(JsonFieldType.NUMBER).description("용량"),
                fieldWithPath("data[0].isFolder").type(JsonFieldType.BOOLEAN).description("폴더/파일 구분")
            )
        ));
    }

    @Test
    @DisplayName("폴더 생성 테스트")
    void createFolder() throws Exception {

    }

    @Test
    void createFile() {
    }
}