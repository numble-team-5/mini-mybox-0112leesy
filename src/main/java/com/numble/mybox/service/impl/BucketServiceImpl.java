package com.numble.mybox.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.data.entity.Bucket;
import com.numble.mybox.data.repository.BucketRepository;
import com.numble.mybox.service.BucketService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BucketServiceImpl implements BucketService {

    private final Logger LOGGER = LoggerFactory.getLogger(BucketServiceImpl.class);
    private final AmazonS3 amazonS3;
    private final BucketRepository bucketRepository;

    @Autowired
    public BucketServiceImpl(AmazonS3 amazonS3, BucketRepository bucketRepository) {
        this.amazonS3 = amazonS3;
        this.bucketRepository = bucketRepository;
    }

    @Override
    public String createBucket() throws RuntimeException {
        String bucketName = UUID.randomUUID().toString();
        errorIfBucketExists(bucketName);

        // create bucket if the bucket name does not exist
        try {
            amazonS3.createBucket(bucketName);
            Bucket bucket = Bucket.builder()
                .bucketName(bucketName)
                .capacity(30.0)
                .build();
            bucketRepository.save(bucket);
            LOGGER.info(String.format("Bucket [%s] has been created.\n", bucketName));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return bucketName;
    }

    @Override
    public BucketResponseDto assignBucket(String username, String bucketName) throws RuntimeException {
        errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setUsername(username);
        Bucket savedBucket = bucketRepository.save(bucket);
        LOGGER.info(
            String.format("Bucket [%s] assigned to User [%s].\n", bucketName, username));
        BucketResponseDto bucketResponseDto = bucketToBucketResponseDto(savedBucket);
        return bucketResponseDto;
    }

    @Override
    public boolean isCapacityEnough(String bucketName, Double size) {
        errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        if (bucket.getCapacity() >= size) {
            return true;
        }
        return false;
    }

    @Override
    public BucketResponseDto increaseCapacity(String bucketName, Double size) {
        errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setCapacity(bucket.getCapacity() + size);
        Bucket savedBucket = bucketRepository.save(bucket);
        LOGGER.info(
            String.format("Bucket [%s] capacity increased %f.\n", bucketName, size));
        BucketResponseDto bucketResponseDto = bucketToBucketResponseDto(savedBucket);
        return bucketResponseDto;
    }

    @Override
    public BucketResponseDto decreaseCapacity(String bucketName, Double size) {
        errorIfBucketNotExists(bucketName);
        Bucket bucket = bucketRepository.getByBucketName(bucketName);
        bucket.setCapacity(bucket.getCapacity() - size);
        Bucket savedBucket = bucketRepository.save(bucket);
        LOGGER.info(
            String.format("Bucket [%s] capacity decreased %f.\n", bucketName, size));
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

    private void errorIfBucketExists(String bucketName) throws RuntimeException {
        if (amazonS3.doesBucketExistV2(bucketName)) {
            LOGGER.info(String.format("Bucket [%s] already exists.\n", bucketName));
            throw new RuntimeException();
        }
    }

    private void errorIfBucketNotExists(String bucketName) throws RuntimeException {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            LOGGER.info(String.format("Bucket [%s] doesn't exist.\n", bucketName));
            throw new RuntimeException();
        }
    }

}
