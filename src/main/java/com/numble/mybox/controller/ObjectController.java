package com.numble.mybox.controller;

import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.service.ObjectService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @GetMapping("/bucket")
    public ResponseEntity<Map<String, List<Object>>> getRootObject(@RequestParam String bucketName,
        String parentPath) {
        Map<String, List<Object>> result = new HashMap<>();
        result.put("data", objectService.getObjects(bucketName, parentPath));

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/folder")
    public ResponseEntity<Object> createFolder(@ModelAttribute ObjectRequestDto objectRequestDto) {

        Object newObject = objectService.createFolder(objectRequestDto);
        return ResponseEntity.ok().body(newObject);
    }

    @PostMapping("/file")
    public ResponseEntity<Object> createFile(@ModelAttribute FileRequestDto fileRequestDto)
        throws IOException {

        Object newObject = objectService.createFile(fileRequestDto);
        return ResponseEntity.ok().body(newObject);
    }

}
