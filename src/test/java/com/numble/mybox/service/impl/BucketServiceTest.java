package com.numble.mybox.service.impl;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.data.entity.Bucket;
import com.numble.mybox.data.repository.BucketRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({BucketServiceImpl.class})
class BucketServiceTest {

    @MockBean
    AmazonS3 amazonS3;
    @MockBean
    BucketRepository bucketRepository;

    @Autowired
    BucketServiceImpl bucketService;

    final String BUCKET_NAME = "test-bucket";
    final String USERNAME = "test-user";

    @Test
    @DisplayName("버킷생성 성공 테스트")
    void createBucketSuccessTest() {
        // given
        Mockito.when(amazonS3.doesBucketExistV2(any(String.class)))
            .thenReturn(false);

        // when
        String bucketName = bucketService.createBucket();

        // then
        verify(amazonS3).doesBucketExistV2(any(String.class));
    }

    @Test
    @DisplayName("버킷생성 실패 테스트")
    void createBucketFailTest() {
        // given
        Mockito.when(amazonS3.doesBucketExistV2(any()))
            .thenReturn(true);

        // when
        Assertions.assertThrows(RuntimeException.class, () -> {
            bucketService.createBucket();
        });

        // then
        verify(amazonS3).doesBucketExistV2(any(String.class));
    }

    @Test
    @DisplayName("버킷지정 성공 테스트")
    void assignBucketSuccessTest() {
        // given
        Mockito.when(amazonS3.doesBucketExistV2(BUCKET_NAME))
            .thenReturn(true);
        Mockito.when(bucketRepository.getByBucketName(BUCKET_NAME))
            .thenReturn(Bucket.builder().bucketName(BUCKET_NAME).capacity(30.0).build());
        Mockito.when(bucketRepository.save(any(Bucket.class))).then(returnsFirstArg());

        // when
        BucketResponseDto bucketResponseDto = bucketService.assignBucket(USERNAME, BUCKET_NAME);

        // then
        Assertions.assertEquals(bucketResponseDto.getUsername(), USERNAME);
        Assertions.assertEquals(bucketResponseDto.getBucketName(), BUCKET_NAME);
        Assertions.assertEquals(bucketResponseDto.getRemain(), 30.0);

        verify(amazonS3).doesBucketExistV2(BUCKET_NAME);
    }

    @Test
    @DisplayName("버킷지정 실패 테스트")
    void assignBucketFailTest() {
        // given
        Mockito.when(amazonS3.doesBucketExistV2(BUCKET_NAME))
            .thenReturn(false);

        // when
        Assertions.assertThrows(RuntimeException.class, () -> {
            bucketService.assignBucket(USERNAME, BUCKET_NAME);
        });

        verify(amazonS3).doesBucketExistV2(BUCKET_NAME);
    }
}