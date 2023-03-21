package com.numble.mybox.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.numble.mybox.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {

    private final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);
    private final AmazonS3 amazonS3;

    @Autowired
    public StorageServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void createBucket(String bucket) {
        // generate unique bucket names using UUID

        try {
            // create bucket if the bucket name does not exist
            if (amazonS3.doesBucketExistV2(bucket)) {
                LOGGER.info(String.format("Bucket [%s] already exists.\n", bucket));
            } else {
                amazonS3.createBucket(bucket);
                LOGGER.info(String.format("Bucket [%s] has been created.\n", bucket));
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

}
