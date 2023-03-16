package com.numble.mybox.controller;

import com.numble.mybox.data.entity.Test;
import com.numble.mybox.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping(value = "/api/test")
    public List<Test> getTestList() {
        return testService.getTestList();
    }

}
