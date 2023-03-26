package com.numble.mybox.data.repository;

import com.numble.mybox.data.entity.Bucket;
import com.numble.mybox.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BucketRepository extends JpaRepository<Bucket, Long> {

    Bucket getByBucketName(String bucketName);

}
