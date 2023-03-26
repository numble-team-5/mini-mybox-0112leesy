package com.numble.mybox.service;

import com.numble.mybox.data.dto.BucketResponseDto;

public interface BucketService {

    public String createBucket();

    public BucketResponseDto assignBucket(String username, String bucket);

}
