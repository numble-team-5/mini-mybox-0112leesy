package com.numble.mybox.service;

import com.numble.mybox.data.entity.Test;
import com.numble.mybox.data.repository.TestRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TestService {
    private TestRepository testRepository;

    public List<Test> getTestList() {
        return testRepository.findAll();
    }

}
