package com.numble.mybox.service;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.numble.mybox.data.entity.Bucket;
import java.util.List;

public interface StorageService {

    public List<Bucket> listBucket();

    public String putBucket();

    public void deleteBucket();

    public void getObject();

    public void putObject(PutObjectRequest putObjectRequest, String path);

    public void downloadFolder();

    public void deleteFolder(String bucketName, String folderPath);

    public void downloadFile();

    public void deleteObject(String bucketName, String path);

    public void errorIfBucketExists(String bucketName);

    public void errorIfBucketNotExists(String bucketName);

}
