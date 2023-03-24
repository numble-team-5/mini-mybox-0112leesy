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
        try {
            // create bucket if the bucket name does not exist
            if (amazonS3.doesBucketExistV2(bucketName)) {
                LOGGER.info(String.format("Bucket [%s] already exists.\n", bucketName));
                throw new RuntimeException();
            } else {
                amazonS3.createBucket(bucketName);
                Bucket bucket = Bucket.builder()
                    .bucketName(bucketName)
                    .remain(30.0)
                    .build();
                bucketRepository.save(bucket);
                LOGGER.info(String.format("Bucket [%s] has been created.\n", bucketName));
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
        return bucketName;
    }

    @Override
    public BucketResponseDto assignBucket(String username, String bucketName) throws RuntimeException {
        BucketResponseDto bucketResponseDto = new BucketResponseDto();
        try {
            if (!amazonS3.doesBucketExistV2(bucketName)) {
                LOGGER.info(String.format("Bucket [%s] not found.\n", bucketName));
                throw new RuntimeException();
            } else {
                Bucket bucket = bucketRepository.getByBucketName(bucketName);
                bucket.setUsername(username);
                Bucket savedBucket = bucketRepository.save(bucket);
                LOGGER.info(String.format("Bucket [%s] assigned to User [%s].\n", bucketName, username));
                bucketResponseDto.setBucketName(savedBucket.getBucketName());
                bucketResponseDto.setUsername(savedBucket.getUsername());
                bucketResponseDto.setRemain(savedBucket.getRemain());
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
        return bucketResponseDto;
    }

}
