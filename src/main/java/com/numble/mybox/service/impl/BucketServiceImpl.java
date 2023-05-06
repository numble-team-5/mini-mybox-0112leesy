package com.numble.mybox.service.impl;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.data.entity.Bucket;
import com.numble.mybox.data.repository.BucketRepository;
import com.numble.mybox.service.BucketService;
import com.numble.mybox.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BucketServiceImpl implements BucketService {

    private final StorageService storageService;
    private final BucketRepository bucketRepository;

    @Autowired
    public BucketServiceImpl(StorageService storageService, BucketRepository bucketRepository) {
        this.storageService = storageService;
        this.bucketRepository = bucketRepository;
    }

    @Override
    public String createBucket() throws RuntimeException {
        String bucketName = storageService.putBucket();
        Bucket bucket = Bucket.builder()
            .bucketName(bucketName)
            .capacity(30.0)
            .build();
        bucketRepository.save(bucket);
        return bucketName;
    }

    @Override
    public BucketResponseDto assignBucket(String username, String bucketName) throws RuntimeException {
        storageService.errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setUsername(username);
        Bucket savedBucket = bucketRepository.save(bucket);
        log.info("Bucket {} assigned to User {}.", bucketName, username);
        BucketResponseDto bucketResponseDto = bucketToBucketResponseDto(savedBucket);
        return bucketResponseDto;
    }

    @Override
    public boolean isCapacityEnough(String bucketName, Double size) {
        storageService.errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        if (bucket.getCapacity() >= size) {
            return true;
        }
        return false;
    }

    @Override
    public BucketResponseDto increaseCapacity(String bucketName, Double size) {
        storageService.errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setCapacity(bucket.getCapacity() + size);
        Bucket savedBucket = bucketRepository.save(bucket);
        log.info("Bucket {} capacity increased {}.", bucketName, size);
        BucketResponseDto bucketResponseDto = bucketToBucketResponseDto(savedBucket);
        return bucketResponseDto;
    }

    @Override
    public BucketResponseDto decreaseCapacity(String bucketName, Double size) {
        storageService.errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setCapacity(bucket.getCapacity() - size);
        Bucket savedBucket = bucketRepository.save(bucket);
        log.info("Bucket {} capacity decreased {}.", bucketName, size);
        BucketResponseDto bucketResponseDto = bucketToBucketResponseDto(savedBucket);
        return bucketResponseDto;
    }

    private BucketResponseDto bucketToBucketResponseDto(Bucket bucket) {
        BucketResponseDto bucketResponseDto = BucketResponseDto.builder()
            .username(bucket.getUsername())
            .bucketName(bucket.getBucketName())
            .capacity(bucket.getCapacity())
            .build();

        return bucketResponseDto;
    }

}
