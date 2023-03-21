package com.numble.mybox;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class TestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("RestDoc 테스트")
    void RestDocTest() throws Exception {
        // given
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "testId");
        jsonObject.put("pw", "testPw");

        // when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/api/test/rest_docs").contentType(
            MediaType.APPLICATION_JSON).content(jsonObject.toString()));

        // then
        // actions.andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200"));
        actions.andExpect(MockMvcResultMatchers.status().isOk());

        // docs
        actions.andDo(document("test-api",
            preprocessRequest(prettyPrint()), //request json 형식으로 이쁘게 출력
            preprocessResponse(prettyPrint()),  //response json 형식으로 이쁘게 출력
            requestFields(  //request parameter
                fieldWithPath("id").type(JsonFieldType.STRING).description("아이디"),
                fieldWithPath("pw").type(JsonFieldType.STRING).description("비밀번호")
            ),
            responseFields( //response parameter
                fieldWithPath("id").type(JsonFieldType.STRING).description("아이디"),
                fieldWithPath("pw").type(JsonFieldType.STRING).description("비밀번호")
            )
        ));
    }
}
