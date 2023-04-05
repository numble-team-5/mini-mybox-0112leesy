package com.numble.mybox.service.impl;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.data.repository.ObjectRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    BucketServiceImpl bucketService;
    @MockBean
    ObjectRepository objectRepository;

    @Autowired
    ObjectServiceImpl objectService;

    @Test
    @DisplayName("루트 오브젝트 조회 테스트")
    void getRootObjectsTest() {
        // given
        Object object1 = Object.builder()
            .id(1L)
            .name("folder/")
            .path("folder/")
            .parentPath("")
            .bucketName("test-bucket")
            .size(0.0)
            .isFolder(true)
            .build();

        Object object2 = Object.builder()
            .id(2L)
            .name("image.jpg")
            .path("image.jpg")
            .parentPath("")
            .bucketName("test-bucket")
            .size(2.3)
            .isFolder(false)
            .build();

        Mockito.when(objectRepository.findByBucketNameAndParentPath("test-bucket", ""))
                .thenReturn(Lists.newArrayList(object1, object2));

        // when
        List<Object> rootObjects = objectService.getObjects("test-bucket", "");

        // then
        Assertions.assertEquals(rootObjects.size(), 2);
        Assertions.assertEquals(rootObjects.get(0).getParentPath(), "");
        Assertions.assertEquals(rootObjects.get(1).getParentPath(), "");
        Assertions.assertEquals(rootObjects.get(0).getBucketName(), "test-bucket");
        Assertions.assertEquals(rootObjects.get(1).getBucketName(), "test-bucket");

        verify(objectRepository).findByBucketNameAndParentPath("test-bucket", "");
    }

    @Test
    @DisplayName("폴더 내 오브젝트 조회 테스트")
    void getObjectsInFolderTest() {
        // given
        Object object1 = Object.builder()
            .id(1L)
            .name("folder/")
            .path("parent-folder/folder/")
            .parentPath("parent-folder/")
            .bucketName("test-bucket")
            .size(0.0)
            .isFolder(true)
            .build();

        Object object2 = Object.builder()
            .id(2L)
            .name("image.jpg")
            .path("parent-folder/image.jpg")
            .parentPath("parent-folder/")
            .bucketName("test-bucket")
            .size(2.3)
            .isFolder(false)
            .build();

        Mockito.when(objectRepository.findByBucketNameAndParentPath("test-bucket", "parent-folder/"))
            .thenReturn(Lists.newArrayList(object1, object2));

        // when
        List<Object> objectsInFolder = objectService.getObjects("test-bucket", "parent-folder/");

        // then
        Assertions.assertEquals(objectsInFolder.size(), 2);
        Assertions.assertEquals(objectsInFolder.get(0).getParentPath(), "parent-folder/");
        Assertions.assertEquals(objectsInFolder.get(1).getParentPath(), "parent-folder/");
        Assertions.assertTrue(objectsInFolder.get(0).getPath().contains("parent-folder/"));
        Assertions.assertTrue(objectsInFolder.get(1).getPath().contains("parent-folder/"));
        Assertions.assertEquals(objectsInFolder.get(0).getBucketName(), "test-bucket");
        Assertions.assertEquals(objectsInFolder.get(1).getBucketName(), "test-bucket");

        verify(objectRepository).findByBucketNameAndParentPath("test-bucket", "parent-folder/");
    }

    @Test
    @DisplayName("폴더 생성 성공 테스트")
    void createFolderSuccessTest() {
        // given
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket", "depth-1/depth-2/"))
            .thenReturn(new ArrayList());
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket", "depth-1/"))
            .thenReturn(Lists.newArrayList(new Object()));

        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name("depth-2/")
            .parentPath("depth-1/")
            .bucketName("test-bucket")
            .build();

        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        ObjectResponseDto newFolder = objectService.createFolder(objectRequestDto);

        // then
        Assertions.assertEquals(newFolder.getName(), "depth-2/");
        Assertions.assertEquals(newFolder.getPath(), "depth-1/depth-2/");
        Assertions.assertEquals(newFolder.getParentPath(), "depth-1/");
        Assertions.assertEquals(newFolder.getBucketName(), "test-bucket");
        Assertions.assertEquals(newFolder.getSize(), 0.0);
        Assertions.assertEquals(newFolder.getIsFolder(), true);
        Assertions.assertEquals(newFolder.getCode(), 0);
        Assertions.assertEquals(newFolder.getMsg(), "정상적으로 처리되었습니다.");

        verify(objectRepository).save(any(Object.class));
        verify(objectRepository).findByBucketNameAndPath("test-bucket","depth-1/depth-2/");
        verify(objectRepository).findByBucketNameAndPath("test-bucket", "depth-1/");
    }

    @Test
    @DisplayName("폴더 경로 중복 테스트")
    void duplicateFolderPathTest() {
        // given
        Object objectWithPath = Object.builder()
            .id(1L)
            .name("root-folder/")
            .path("root-folder/")
            .parentPath("")
            .bucketName("test-bucket")
            .size(0.0)
            .isFolder(true)
            .build();

        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket","root-folder/"))
            .thenReturn(Lists.newArrayList(objectWithPath));

        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name("root-folder/")
            .parentPath("")
            .bucketName("test-bucket")
            .build();

        // when
        ObjectResponseDto objectResponseDto = objectService.createFolder(objectRequestDto);

        // then
        Assertions.assertNull(objectResponseDto.getName());
        Assertions.assertEquals(objectResponseDto.getCode(), -1);
        Assertions.assertEquals(objectResponseDto.getMsg(), "이미 존재하는 파일 또는 폴더입니다.");

        verify(objectRepository).findByBucketNameAndPath("test-bucket","root-folder/");
    }

    @Test
    @DisplayName("텍스트 파일 생성 테스트")
    void createTextFileTest() throws IOException {
        // given
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket","textFile.txt"))
            .thenReturn(new ArrayList());

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getContentType()).thenReturn("text/plain");
        Mockito.when(multipartFile.getOriginalFilename()).thenReturn("textFile.txt");
        String text = "This is sample text.";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        Mockito.when(multipartFile.getSize()).thenReturn((long) text.getBytes().length);
        Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(multipartFile)
            .parentPath("")
            .bucketName("test-bucket")
            .build();

        Mockito.when(
                bucketService.isCapacityEnough("test-bucket", text.getBytes().length / 1024.0 / 1024.0))
            .thenReturn(true);
        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        ObjectResponseDto newFile = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertEquals(newFile.getName(), "textFile.txt");
        Assertions.assertEquals(newFile.getPath(), "textFile.txt");
        Assertions.assertEquals(newFile.getParentPath(), "");
        Assertions.assertEquals(newFile.getBucketName(), "test-bucket");
        Assertions.assertEquals(newFile.getSize(), text.getBytes().length / 1024.0 / 1024.0);
        Assertions.assertEquals(newFile.getIsFolder(), false);
        Assertions.assertEquals(newFile.getCode(), 0);
        Assertions.assertEquals(newFile.getMsg(), "정상적으로 처리되었습니다.");

        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(objectRepository).save(any(Object.class));
        verify(bucketService).isCapacityEnough("test-bucket", text.getBytes().length / 1024.0 / 1024.0);
        verify(bucketService).decreaseCapacity(any(), any());
        verify(objectRepository).findByBucketNameAndPath("test-bucket", "textFile.txt");
    }

    @Test
    @DisplayName("이미지 파일 생성 테스트")
    void createImageFileTest() throws IOException {
        // given
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket","depth-1/testImage1.jpeg"))
            .thenReturn(new ArrayList());
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket", "depth-1/"))
            .thenReturn(Lists.newArrayList(new Object()));

        String originalFilename = "testImage1.jpeg";
        MockMultipartFile file = new MockMultipartFile("multipartFile",
            originalFilename,
            "image/jpg",
            new FileInputStream("src/test/resources/" + originalFilename));

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(file)
            .parentPath("depth-1/")
            .bucketName("test-bucket")
            .build();

        Mockito.when(bucketService.isCapacityEnough(any(), any())).thenReturn(true);
        Mockito.when(objectRepository.save(any(Object.class))).then(returnsFirstArg());

        // when
        ObjectResponseDto newFile = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertEquals(newFile.getName(), originalFilename);
        Assertions.assertEquals(newFile.getPath(), "depth-1/" + originalFilename);
        Assertions.assertEquals(newFile.getParentPath(), "depth-1/");
        Assertions.assertEquals(newFile.getBucketName(), "test-bucket");
        Assertions.assertEquals((int) Math.round(newFile.getSize()), 2);
        Assertions.assertEquals(newFile.getIsFolder(), false);
        Assertions.assertEquals(newFile.getCode(), 0);
        Assertions.assertEquals(newFile.getMsg(), "정상적으로 처리되었습니다.");

        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(objectRepository).save(any(Object.class));
        verify(bucketService).isCapacityEnough(any(), any());
        verify(bucketService).decreaseCapacity(any(), any());
        verify(objectRepository).findByBucketNameAndPath("test-bucket","depth-1/testImage1.jpeg");
        verify(objectRepository).findByBucketNameAndPath("test-bucket", "depth-1/");
    }

    @Test
    @DisplayName("파일 경로 중복 테스트")
    void duplicateFilePathTest() throws IOException {
        // given
        Object objectWithPath = Object.builder()
            .id(1L)
            .name("textFile.txt")
            .path("textFile.txt")
            .parentPath("")
            .bucketName("test-bucket")
            .size(1.0)
            .isFolder(false)
            .build();

        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket","textFile.txt"))
            .thenReturn(Lists.newArrayList(objectWithPath));

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getContentType()).thenReturn("text/plain");
        Mockito.when(multipartFile.getOriginalFilename()).thenReturn("textFile.txt");
        String text = "This is sample text.";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        Mockito.when(multipartFile.getSize()).thenReturn((long) text.getBytes().length);
        Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(multipartFile)
            .parentPath("")
            .bucketName("test-bucket")
            .build();

        // when
        ObjectResponseDto objectResponseDto = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertNull(objectResponseDto.getName());
        Assertions.assertEquals(objectResponseDto.getCode(), -1);
        Assertions.assertEquals(objectResponseDto.getMsg(), "이미 존재하는 파일 또는 폴더입니다.");

        verify(objectRepository).findByBucketNameAndPath("test-bucket", "textFile.txt");
    }

    @Test
    @DisplayName("상위 폴더 없음 테스트")
    void parentPathNotFoundTest() throws IOException {
        // given
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket", "depth-1/depth-2/"))
            .thenReturn(new ArrayList());
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket", "depth-1/"))
            .thenReturn(new ArrayList());

        ObjectRequestDto objectRequestDto = ObjectRequestDto.builder()
            .name("depth-2/")
            .parentPath("depth-1/")
            .bucketName("test-bucket")
            .build();

        // when
        ObjectResponseDto objectResponseDto = objectService.createFolder(objectRequestDto);

        // then
        Assertions.assertNull(objectResponseDto.getName());
        Assertions.assertEquals(objectResponseDto.getCode(), -1);
        Assertions.assertEquals(objectResponseDto.getMsg(), "상위 폴더가 존재하지 않습니다.");

        verify(objectRepository).findByBucketNameAndPath("test-bucket", "depth-1/depth-2/");
        verify(objectRepository).findByBucketNameAndPath("test-bucket", "depth-1/");
    }

    @Test
    @DisplayName("용량 부족 테스트")
    void capacityNotEnoughTest() throws IOException {
        // given
        Mockito.when(objectRepository.findByBucketNameAndPath("test-bucket","textFile.txt"))
            .thenReturn(new ArrayList());

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
        Mockito.when(multipartFile.getContentType()).thenReturn("text/plain");
        Mockito.when(multipartFile.getOriginalFilename()).thenReturn("textFile.txt");
        String text = "This is sample text.";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        Mockito.when(multipartFile.getSize()).thenReturn((long) text.getBytes().length);
        Mockito.when(multipartFile.getInputStream()).thenReturn(inputStream);

        FileRequestDto fileRequestDto = FileRequestDto.builder()
            .multipartFile(multipartFile)
            .parentPath("")
            .bucketName("test-bucket")
            .build();

        Mockito.when(
                bucketService.isCapacityEnough("test-bucket", text.getBytes().length / 1024.0 / 1024.0))
            .thenReturn(false);

        // when
        ObjectResponseDto objectResponseDto = objectService.createFile(fileRequestDto);

        // then
        Assertions.assertNull(objectResponseDto.getName());
        Assertions.assertEquals(objectResponseDto.getCode(), -1);
        Assertions.assertEquals(objectResponseDto.getMsg(), "사용 가능한 용량이 충분하지 않습니다.");

        verify(objectRepository).findByBucketNameAndPath("test-bucket", "textFile.txt");
        verify(bucketService).isCapacityEnough("test-bucket", text.getBytes().length / 1024.0 / 1024.0);
    }


}