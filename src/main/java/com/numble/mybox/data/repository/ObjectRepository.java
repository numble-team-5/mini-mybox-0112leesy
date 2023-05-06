package com.numble.mybox.data.repository;

import com.numble.mybox.data.entity.Object;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectRepository extends JpaRepository<Object, Long> {

    List<Object> findByBucketNameAndParentPath(String bucketName, String parentPath);
    Object findByBucketNameAndPath(String bucketName, String path);
    void deleteByBucketNameAndPath(String bucketName, String path);
    void deleteByBucketNameAndParentPath(String bucketName, String parentPath);

}
