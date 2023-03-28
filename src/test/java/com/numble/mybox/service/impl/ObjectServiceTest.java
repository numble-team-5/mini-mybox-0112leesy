package com.numble.mybox.service.impl;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.data.repository.ObjectRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

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

        Object object1 = Object.builder()
            .id(1L)
            .name("folder/")
            .fullName("folder/")
            .parentFullName(null)
            .bucketName("test-bucket")
            .size(0.0)
            .isFolder(true)
            .build();

        Object object2 = Object.builder()
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
        // given
        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name("root-folder/")
            .parentFullName(null)
            .bucketName("test-bucket")
            .build();

        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        Object newFolder = objectService.createFolder(objectRequestDto);

        // then
        Assertions.assertEquals(newFolder.getName(), "root-folder/");
        Assertions.assertEquals(newFolder.getFullName(), "root-folder/");
        Assertions.assertNull(newFolder.getParentFullName());
        Assertions.assertEquals(newFolder.getBucketName(), "test-bucket");
        Assertions.assertEquals(newFolder.getSize(), 0.0);
        Assertions.assertEquals(newFolder.getIsFolder(), true);

        verify(objectRepository).save(any(Object.class));
    }

    @Test
    @DisplayName("폴더 내 폴더 생성 테스트")
    void createFolderInFolderTest() {
        // given
        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name("depth-2/")
            .parentFullName("depth-1/")
            .bucketName("test-bucket")
            .build();

        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        Object newFolder = objectService.createFolder(objectRequestDto);

        // then
        Assertions.assertEquals(newFolder.getName(), "depth-2/");
        Assertions.assertEquals(newFolder.getFullName(), "depth-1/depth-2/");
        Assertions.assertEquals(newFolder.getParentFullName(), "depth-1/");
        Assertions.assertEquals(newFolder.getBucketName(), "test-bucket");
        Assertions.assertEquals(newFolder.getSize(), 0.0);
        Assertions.assertEquals(newFolder.getIsFolder(), true);

        verify(objectRepository).save(any(Object.class));
    }

    @Test
    @DisplayName("루트에 텍스트 파일 생성 테스트")
    void createTextFileInRootTest() throws IOException {
        // given
        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getContentType()).thenReturn("text/plain");
        Mockito.when(multipartFile.getOriginalFilename()).thenReturn("textFile.txt");
        String text = "This is sample text.";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        Mockito.when(multipartFile.getSize()).thenReturn((long) text.getBytes().length);
        Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(multipartFile)
            .parentFullName(null)
            .bucketName("test-bucket")
            .build();

        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        Object newFile = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertEquals(newFile.getName(), "textFile.txt");
        Assertions.assertEquals(newFile.getFullName(), "textFile.txt");
        Assertions.assertNull(newFile.getParentFullName());
        Assertions.assertEquals(newFile.getBucketName(), "test-bucket");
        Assertions.assertEquals(newFile.getSize(), text.getBytes().length / 1024.0 / 1024.0);
        Assertions.assertEquals(newFile.getIsFolder(), false);

        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(objectRepository).save(any(Object.class));
    }

    @Test
    @DisplayName("폴더에 이미지 파일 생성 테스트")
    void createImageFileInFolderTest() throws IOException {
        // given
        String originalFilename = "testImage1.jpeg";
        MockMultipartFile file = new MockMultipartFile("multipartFile",
            originalFilename,
            "image/jpg",
            new FileInputStream("src/test/resources/" + originalFilename));

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(file)
            .parentFullName("depth-1/")
            .bucketName("test-bucket")
            .build();

        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        Object newFile = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertEquals(newFile.getName(), originalFilename);
        Assertions.assertEquals(newFile.getFullName(), "depth-1/" + originalFilename);
        Assertions.assertEquals(newFile.getParentFullName(), "depth-1/");
        Assertions.assertEquals(newFile.getBucketName(), "test-bucket");
        Assertions.assertEquals((int) Math.round(newFile.getSize()), 2);
        Assertions.assertEquals(newFile.getIsFolder(), false);

        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(objectRepository).save(any(Object.class));
    }
}