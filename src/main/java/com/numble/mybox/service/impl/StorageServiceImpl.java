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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
    public String putBucket() {
        String bucketName = UUID.randomUUID().toString();
        try {
            amazonS3.createBucket(bucketName);
            log.info("Bucket {} has been created.", bucketName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return bucketName;
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
            log.info("Object {} has been created.", path);
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
            log.info("Object {} has been deleted.", path);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void errorIfBucketExists(String bucketName) throws RuntimeException {
        if (amazonS3.doesBucketExistV2(bucketName)) {
            log.info("Bucket {} already exists.", bucketName);
            throw new RuntimeException();
        }
    }

    @Override
    public void errorIfBucketNotExists(String bucketName) throws RuntimeException {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            log.info("Bucket {} doesn't exist.", bucketName);
            throw new RuntimeException();
        }
    }
}
