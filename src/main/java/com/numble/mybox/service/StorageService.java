package com.numble.mybox.service;

import com.numble.mybox.data.entity.Bucket;
import java.util.List;

public interface StorageService {

    public List<Bucket> listBucket();

    public void putBucket();

    public void deleteBucket();

    public void getObject();

    public void createFolder();

    public void downloadFolder();

    public void deleteFolder();

    public void uploadFile();

    public void downloadFile();

    public void deleteFile();

}
