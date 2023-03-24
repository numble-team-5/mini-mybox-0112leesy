package com.numble.mybox.controller;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.service.BucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bucket-api")
public class BucketController {

    private final Logger LOGGER = LoggerFactory.getLogger(BucketController.class);
    private final BucketService bucketService;

    @Autowired
    public BucketController(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @PostMapping(value = "/bucket")
    public BucketResponseDto assignNewBucket(@RequestParam String username) throws RuntimeException {
        String bucketName = bucketService.createBucket();
        bucketService.assignBucket(username, bucketName);
        BucketResponseDto bucketResponseDto = BucketResponseDto.builder()
            .username(username)
            .bucketName(bucketName)
            .remain(30.0)
            .build();
        LOGGER.info("[assignNewBucket] Bucket [{}]를 생성하여 User {} 에 등록하였습니다.", bucketName, username);
        return bucketResponseDto;
    }

}
