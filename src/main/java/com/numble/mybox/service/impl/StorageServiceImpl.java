package com.numble.mybox.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.numble.mybox.data.entity.Bucket;
import com.numble.mybox.service.StorageService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {

    private final AmazonS3 amazonS3;

    @Autowired
    public StorageServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }


    @Override
    public List<Bucket> listBucket() {
        return null;
    }

    @Override
    public void putBucket() {
        String bucketName = UUID.randomUUID().toString();
        try {
            amazonS3.createBucket(bucketName);
//            Bucket bucket = Bucket.builder()
//                .bucketName(bucketName)
//                .capacity(30.0)
//                .build();
            // LOGGER.info(String.format("Bucket [%s] has been created.\n", bucketName));
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteBucket() {

    }

    @Override
    public void getObject() {

    }

    @Override
    public void putObject(PutObjectRequest putObjectRequest, String path) {
        try {
            amazonS3.putObject(putObjectRequest);
            System.out.format("Object %s has been created.\n", path);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadFolder() {

    }

    @Override
    public void deleteFolder(String bucketName, String folderPath) {
        List<S3ObjectSummary> fileList = amazonS3.listObjects(bucketName, folderPath).getObjectSummaries();
        for (S3ObjectSummary file : fileList) {
            deleteObject(bucketName, file.getKey());
        }
        deleteObject(bucketName, folderPath);
    }

    @Override
    public void downloadFile() {

    }

    @Override
    public void deleteObject(String bucketName, String path) {
        try {
            amazonS3.deleteObject(bucketName, path);
            System.out.format("Object %s has been deleted.\n", path);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void errorIfBucketExists(String bucketName) throws RuntimeException {
        if (amazonS3.doesBucketExistV2(bucketName)) {
            // LOGGER.info(String.format("Bucket [%s] already exists.\n", bucketName));
            throw new RuntimeException();
        }
    }

    @Override
    public void errorIfBucketNotExists(String bucketName) throws RuntimeException {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            // LOGGER.info(String.format("Bucket [%s] doesn't exist.\n", bucketName));
            throw new RuntimeException();
        }
    }
}
