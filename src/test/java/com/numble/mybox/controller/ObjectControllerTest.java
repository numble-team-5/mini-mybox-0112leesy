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
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.service.impl.ObjectServiceImpl;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
    @DisplayName("오브젝트 조회 테스트")
    void getRootObject() throws Exception {
        // given
        String bucketName = "test-bucket";
        String parentPath = "folder/";
        List<Object> rootObjects = new ArrayList<>();
        Object rootImageObject = Object.builder()
            .id(1L)
            .name("image1.jpg")
            .path("folder/image1.jpg")
            .parentPath(parentPath)
            .bucketName(bucketName)
            .size(2.3)
            .isFolder(false)
            .build();
        Object rootFolderObject = Object.builder()
            .id(2L)
            .name("folder-depth2/")
            .path("folder/folder-depth2/")
            .parentPath(parentPath)
            .bucketName(bucketName)
            .size(0.0)
            .isFolder(true)
            .build();
        rootObjects.add(rootImageObject);
        rootObjects.add(rootFolderObject);

        given(objectService.getObjects(bucketName, parentPath)).willReturn(rootObjects);

        // when
        ResultActions actions = mockMvc.perform(
            get("/object-api/bucket?bucketName="+bucketName+"&parentPath="+parentPath)
        );

        // then
        String expectByBucketName = "$.data.[?(@.bucketName == '%s')]";
        String expectByParentPath = "$.data.[?(@.parentPath == '%s')]";
        String expectByName = "$.data.[?(@.name == '%s')]";
        String expectBySize = "$.data.[?(@.size == '%s')]";

        actions.andExpect(status().isOk())
            .andExpect(jsonPath(expectByBucketName, bucketName).exists())
            .andExpect(jsonPath(expectByParentPath, parentPath).exists())
            .andExpect(jsonPath(expectByName, "image1.jpg").exists())
            .andExpect(jsonPath(expectByName, "folder-depth2/").exists())
            .andExpect(jsonPath(expectBySize, 2.3).exists())
            .andExpect(jsonPath(expectBySize, 0.0).exists())
            .andDo(print());

        verify(objectService).getObjects(bucketName, parentPath);

        // docs
        actions.andDo(document("get-objects-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("bucketName").description("버킷 이름"),
                parameterWithName("parentPath").description("상위 폴더 경로")
            ),
            responseFields(
                fieldWithPath("data[0].id").type(JsonFieldType.NUMBER).description("인덱스"),
                fieldWithPath("data[0].name").type(JsonFieldType.STRING).description("파일/폴더 이름"),
                fieldWithPath("data[0].name").type(JsonFieldType.STRING).description("파일/폴더 이름"),
                fieldWithPath("data[0].path").type(JsonFieldType.STRING).description("경로"),
                fieldWithPath("data[0].parentPath").type(JsonFieldType.STRING).description("상위 폴더 경로"),
                fieldWithPath("data[0].bucketName").type(JsonFieldType.STRING).description("버킷이름"),
                fieldWithPath("data[0].size").type(JsonFieldType.NUMBER).description("용량"),
                fieldWithPath("data[0].isFolder").type(JsonFieldType.BOOLEAN).description("폴더/파일 구분")
            )
        ));
    }

    @Test
    @DisplayName("폴더 생성 테스트")
    void createFolder() throws Exception {
        // given
        String folderName = "folder-depth-2/";
        String parentPath = "folder-depth-1/";
        String bucketName = "test-bucket";

        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name(folderName)
            .parentPath(parentPath)
            .bucketName(bucketName)
            .build();

        ObjectResponseDto objectResponseDto = ObjectResponseDto.builder()
            .name(folderName)
            .parentPath(parentPath)
            .path(parentPath+folderName)
            .bucketName(bucketName)
            .size(0.0)
            .isFolder(true)
            .code(0)
            .success(true)
            .msg("정상적으로 처리되었습니다.")
            .build();

        given(objectService.createFolder(objectRequestDto)).willReturn(objectResponseDto);

        // when
        ResultActions actions = mockMvc.perform(
            post("/object-api/folder")
                .param("name", folderName)
                .param("parentPath", parentPath)
                .param("bucketName", bucketName));

        // then
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.parentPath").exists())
            .andExpect(jsonPath("$.path").value(parentPath+folderName))
            .andExpect(jsonPath("$.bucketName").exists())
            .andExpect(jsonPath("$.size").exists())
            .andExpect(jsonPath("$.isFolder").exists())
            .andDo(print());

        verify(objectService).createFolder(objectRequestDto);

        // docs
        actions.andDo(document("create-folder-object-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName("name").description("폴더 이름"),
                parameterWithName("parentPath").description("상위 폴더 경로"),
                parameterWithName("bucketName").description("버킷 이름")
            ),
            responseFields(
                fieldWithPath("name").type(JsonFieldType.STRING).description("폴더 이름"),
                fieldWithPath("parentPath").type(JsonFieldType.STRING).description("상위 폴더 경로"),
                fieldWithPath("path").type(JsonFieldType.STRING).description("경로"),
                fieldWithPath("bucketName").type(JsonFieldType.STRING).description("버킷 이름"),
                fieldWithPath("size").type(JsonFieldType.NUMBER).description("용량"),
                fieldWithPath("isFolder").type(JsonFieldType.BOOLEAN).description("폴더 여부"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공여부"),
                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                fieldWithPath("msg").type(JsonFieldType.STRING).description("메시지")
            )
        ));
    }

    @Test
    @DisplayName("파일 생성 테스트")
    void createFile() throws Exception {
        // given
        String bucketName = "test-bucket";
        String originalFilename = "testImage1.jpeg";
        MockMultipartFile file = new MockMultipartFile("multipartFile",
            originalFilename,
            "image/jpg",
            new FileInputStream("src/test/resources/"+originalFilename));

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .bucketName(bucketName)
            .parentPath("")
            .multipartFile(file)
            .build();

        ObjectResponseDto objectResponseDto = ObjectResponseDto.builder()
            .name(originalFilename)
            .parentPath("")
            .path(originalFilename)
            .bucketName(bucketName)
            .size(2.3)
            .isFolder(false)
            .code(0)
            .success(true)
            .msg("정상적으로 처리되었습니다.")
            .build();

        given(objectService.createFile(fileRequestDto)).willReturn(objectResponseDto);

        // when
        ResultActions actions = mockMvc.perform(
            multipart("/object-api/file").file(file)
                    .param("bucketName", bucketName)
                    .param("parentPath", ""));

        // then
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.parentPath").value(""))
            .andExpect(jsonPath("$.path").value(originalFilename))
            .andExpect(jsonPath("$.bucketName").exists())
            .andExpect(jsonPath("$.size").exists())
            .andExpect(jsonPath("$.isFolder").exists())
            .andDo(print());

        verify(objectService).createFile(fileRequestDto);

        // docs
        actions.andDo(document("create-file-object-api",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(
                partWithName("multipartFile").description("이미지 파일")
            ),
            requestParameters(
                parameterWithName("bucketName").description("버킷 이름"),
                parameterWithName("parentPath").description("상위 폴더 경로")
            ),
            responseFields(
                fieldWithPath("name").type(JsonFieldType.STRING).description("파일 이름"),
                fieldWithPath("parentPath").type(JsonFieldType.STRING).description("상위 폴더 경로"),
                fieldWithPath("path").type(JsonFieldType.STRING).description("경로"),
                fieldWithPath("bucketName").type(JsonFieldType.STRING).description("버킷 이름"),
                fieldWithPath("size").type(JsonFieldType.NUMBER).description("용량"),
                fieldWithPath("isFolder").type(JsonFieldType.BOOLEAN).description("폴더 여부"),
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공여부"),
                fieldWithPath("code").type(JsonFieldType.NUMBER).description("코드"),
                fieldWithPath("msg").type(JsonFieldType.STRING).description("메시지")
            )
        ));
    }
}