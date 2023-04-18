package com.numble.mybox.controller;

import com.numble.mybox.data.dto.ErrorResponseDto;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.exception.ObjectAlreadyExistsException;
import com.numble.mybox.service.ObjectService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public ResponseEntity<Map<String, List<Object>>> getObjects(@RequestParam String bucketName,
        String parentPath) throws ObjectNotFoundException {
        Map<String, List<Object>> result = new HashMap<>();
        result.put("data", objectService.getObjects(bucketName, parentPath));

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/folder")
    public ResponseEntity<ObjectResponseDto> createFolder(@ModelAttribute ObjectRequestDto objectRequestDto)
        throws ObjectAlreadyExistsException, ObjectNotFoundException {

        return ResponseEntity.ok().body(objectService.createFolder(objectRequestDto));
    }

    @PostMapping("/file")
    public ResponseEntity<ObjectResponseDto> createFile(@ModelAttribute FileRequestDto fileRequestDto)
        throws IOException, ObjectAlreadyExistsException, ObjectNotFoundException {

        return ResponseEntity.ok().body(objectService.createFile(fileRequestDto));
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleObjectNotFoundException(ObjectNotFoundException e) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleObjectAlreadyExistsException(ObjectAlreadyExistsException e) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

}
