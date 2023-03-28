package com.numble.mybox.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.data.entity.QObject;
import com.numble.mybox.data.repository.ObjectRepository;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({ObjectServiceImpl.class})
class ObjectServiceTest {

    @MockBean
    AmazonS3 amazonS3;
    @MockBean
    ObjectRepository objectRepository;
    @MockBean
    JPAQueryFactory queryFactory;

    @Autowired
    ObjectServiceImpl objectService;

    @Test
    @DisplayName("루트 오브젝트 조회 테스트")
    void getRootObjectTest() {
        // given
        JPAQuery step1 = Mockito.mock(JPAQuery.class);
        JPAQuery step2 = Mockito.mock(JPAQuery.class);

        Object object1 =  Object.builder()
            .id(1L)
            .name("folder/")
            .fullName("folder/")
            .parentFullName(null)
            .bucketName("test-bucket")
            .size(0.0)
            .isFolder(true)
            .build();

        Object object2 =  Object.builder()
            .id(2L)
            .name("image.jpg")
            .fullName("image.jpg")
            .parentFullName(null)
            .bucketName("test-bucket")
            .size(2.3)
            .isFolder(false)
            .build();

        Mockito.when(queryFactory.selectFrom(any())).thenReturn(step1);
        Mockito.when(step1.where(any(Predicate.class))).thenReturn(step2);
        Mockito.when(step2.fetch()).thenReturn(Lists.newArrayList(object1, object2));

        // when
        List<Object> rootObjects = objectService.getRootObject("test-bucket");

        // then
        Assertions.assertEquals(rootObjects.size(), 2);
        Assertions.assertNull(rootObjects.get(0).getParentFullName());
        Assertions.assertNull(rootObjects.get(1).getParentFullName());
        Assertions.assertEquals(rootObjects.get(0).getBucketName(), "test-bucket");
        Assertions.assertEquals(rootObjects.get(1).getBucketName(), "test-bucket");

        verify(queryFactory).selectFrom(any());
        verify(step1).where(any(Predicate.class));
        verify(step2).fetch();
    }

    @Test
    @DisplayName("루트 폴더 생성 테스트")
    void createRootFolderTest() {
    }

    @Test
    @DisplayName("루트 폴더 생성 테스트")
    void createFolderInFolderTest() {
    }

    @Test
    @DisplayName("폴더 생성 실패 테스트")
    void createFolderFailTest() {
    }

    @Test
    void createFile() {
    }
}