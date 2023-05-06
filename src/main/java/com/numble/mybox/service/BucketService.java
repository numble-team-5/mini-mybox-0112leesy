package com.numble.mybox.service;

import com.numble.mybox.data.dto.BucketResponseDto;

public interface BucketService {

    public String createBucket();

    public BucketResponseDto assignBucket(String username, String bucket);

    public boolean isCapacityEnough(String bucketName, Double size);

    public BucketResponseDto increaseCapacity(String bucketName, Double size);

    public BucketResponseDto decreaseCapacity(String bucketName, Double size);

}
