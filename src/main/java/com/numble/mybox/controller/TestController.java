package com.numble.mybox.controller;

import com.numble.mybox.data.dto.RestDocsDto;
import com.numble.mybox.data.entity.Test;
import com.numble.mybox.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {
    private final TestService testService;
    @GetMapping(value = "/api/test")
    public List<Test> getTestList() {
        return testService.getTestList();
    }

    @PostMapping(value = "/api/test/rest_docs")
    public ResponseEntity restDocs(@RequestBody RestDocsDto restDocsDto) {
        return ResponseEntity.status(HttpStatus.OK).body(restDocsDto);
    }

    @GetMapping(value = "/api/for-user")
    public String getUserPage() {
        return "USER PAGE";
    }

    @GetMapping(value = "/api/for-admin")
    public String getAdminPage() {
        return "ADMIN_PAGE";
    }

    @GetMapping(value = "/api/for-public")
    public String getPublicPage() {
        return "PUBLIC_PAGE";
    }
}
