package com.numble.mybox.controller;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.service.BucketService;
import com.numble.mybox.service.ObjectService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/object-api")
public class ObjectController {

    private final Logger LOGGER = LoggerFactory.getLogger(BucketController.class);
    private final ObjectService objectService;

    @Autowired
    public ObjectController(ObjectService objectService) {
        this.objectService = objectService;
    }

    @GetMapping("bucket/{bucketName}")
    public ResponseEntity<Map<String, Object>> getRootObject(@PathVariable String bucketName) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", objectService.getRootObject(bucketName));

        return ResponseEntity.ok().body(result);
    }

}
