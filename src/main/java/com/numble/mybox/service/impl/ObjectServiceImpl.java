package com.numble.mybox.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.numble.mybox.common.CommonResponse;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import com.numble.mybox.data.repository.ObjectRepository;
import com.numble.mybox.service.BucketService;
import com.numble.mybox.service.ObjectService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ObjectServiceImpl implements ObjectService {

    private final Logger LOGGER = LoggerFactory.getLogger(ObjectServiceImpl.class);
    private final AmazonS3 amazonS3;
    private final BucketService bucketService;
    private final ObjectRepository objectRepository;
    // private final JPAQueryFactory queryFactory;

    @Autowired
    public ObjectServiceImpl(AmazonS3 amazonS3, BucketService bucketService,
        ObjectRepository objectRepository) {
        this.amazonS3 = amazonS3;
        this.bucketService = bucketService;
        this.objectRepository = objectRepository;
    }

    @Override
    public List<Object> getObjects(String bucketName, String parentPath) {
        List<Object> objects = objectRepository.findByBucketNameAndParentPath(bucketName,
            parentPath);
        return objects;
    }

    @Override
    public ObjectResponseDto createFolder(ObjectRequestDto objectRequestDto) {
        String path = objectRequestDto.getParentPath() + objectRequestDto.getName();
        if (doesPathExist(objectRequestDto.getBucketName(), path)) {
            ObjectResponseDto objectResponseDto = new ObjectResponseDto();
            setDuplicatePathErrorResult(objectResponseDto);
            return objectResponseDto;
        }

        if (!objectRequestDto.getParentPath().isEmpty() && !doesPathExist(
            objectRequestDto.getBucketName(), objectRequestDto.getParentPath())) {
            ObjectResponseDto objectResponseDto = new ObjectResponseDto();
            setParentPathNotFoundResult(objectResponseDto);
            return objectResponseDto;
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(objectRequestDto.getBucketName(),
            path, new ByteArrayInputStream(new byte[0]), objectMetadata);

        S3PutObject(putObjectRequest, path);

        Object newFolder = Object.builder()
            .bucketName(objectRequestDto.getBucketName())
            .name(objectRequestDto.getName())
            .size(0.0)
            .path(path)
            .parentPath(objectRequestDto.getParentPath())
            .isFolder(true)
            .build();

        ObjectResponseDto objectResponseDto = objectToObjectResponseDto(
            objectRepository.save(newFolder));
        setSuccessResult(objectResponseDto);
        return objectResponseDto;
    }

    @Override
    public ObjectResponseDto createFile(FileRequestDto fileRequestDto) throws IOException {
        MultipartFile multipartFile = fileRequestDto.getMultipartFile();
        String contentType = multipartFile.getContentType();
        String fileName = Normalizer.normalize(multipartFile.getOriginalFilename(),
            Normalizer.Form.NFC);
        String path = fileRequestDto.getParentPath() + fileName;

        ObjectResponseDto objectResponseDto = new ObjectResponseDto();
        if (doesPathExist(fileRequestDto.getBucketName(), path)) {
            setDuplicatePathErrorResult(objectResponseDto);
            return objectResponseDto;
        }

        if (!fileRequestDto.getParentPath().isEmpty() && !doesPathExist(
            fileRequestDto.getBucketName(), fileRequestDto.getParentPath())) {
            setParentPathNotFoundResult(objectResponseDto);
            return objectResponseDto;
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        long fileSize = multipartFile.getSize();
        System.out.format("Object %s has been created.\n", fileName);
        objectMetadata.setContentLength(fileSize);
        double fileSizeMb = fileSize / 1024.0 / 1024.0;
        System.out.format("File size : %.2f mb\n", fileSizeMb);
        objectMetadata.setContentType(contentType);

        if (!bucketService.isCapacityEnough(fileRequestDto.getBucketName(), fileSizeMb)) {
            setCapacityNotEnoughResult(objectResponseDto);
            return objectResponseDto;
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(fileRequestDto.getBucketName(),
            path, multipartFile.getInputStream(), objectMetadata);

        S3PutObject(putObjectRequest, path);

        bucketService.decreaseCapacity(fileRequestDto.getBucketName(), fileSizeMb);

        Object newFile = Object.builder()
            .bucketName(fileRequestDto.getBucketName())
            .name(fileName)
            .size(fileSizeMb)
            .path(path)
            .parentPath(fileRequestDto.getParentPath())
            .isFolder(false)
            .build();

        objectResponseDto = objectToObjectResponseDto(objectRepository.save(newFile));
        setSuccessResult(objectResponseDto);
        return objectResponseDto;
    }

    @Override
    public boolean deleteFolder(ObjectRequestDto objectRequestDto) {
        String folderPath = objectRequestDto.getParentPath() + objectRequestDto.getName();
        S3deleteFolder(objectRequestDto.getBucketName(), folderPath);
        objectRepository.deleteByBucketNameAndPath(objectRequestDto.getBucketName(), folderPath);
        return true;
    }

    @Override
    public boolean deleteFile(ObjectRequestDto objectRequestDto) {
        String filePath = objectRequestDto.getParentPath() + objectRequestDto.getName();
        S3deleteObject(objectRequestDto.getBucketName(), filePath);
        objectRepository.deleteByBucketNameAndPath(objectRequestDto.getBucketName(), filePath);
        return true;
    }

    private void S3PutObject(PutObjectRequest putObjectRequest, String path) {
        try {
            amazonS3.putObject(putObjectRequest);
            System.out.format("Object %s has been created.\n", path);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    private void S3deleteFolder(String bucketName, String folderPath) {
        List<S3ObjectSummary> fileList = amazonS3.listObjects(bucketName, folderPath).getObjectSummaries();
        for (S3ObjectSummary file : fileList) {
            S3deleteObject(bucketName, file.getKey());
        }
        S3deleteObject(bucketName, folderPath);
    }

    private void S3deleteObject(String bucketName, String path) {
        try {
            amazonS3.deleteObject(bucketName, path);
            System.out.format("Object %s has been deleted.\n", path);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch(SdkClientException e) {
            e.printStackTrace();
        }
    }

    private boolean doesPathExist(String bucketName, String path) {
        Object objectWithPath = objectRepository.findByBucketNameAndPath(bucketName, path);
        if (objectWithPath == null) {
            return false;
        }
        return true;
    }

    private ObjectResponseDto objectToObjectResponseDto(Object object) {
        ObjectResponseDto objectResponseDto = ObjectResponseDto.builder()
            .name(object.getName())
            .path(object.getPath())
            .parentPath(object.getParentPath())
            .bucketName(object.getBucketName())
            .size(object.getSize())
            .isFolder(object.getIsFolder())
            .build();

        return objectResponseDto;
    }

    private void setSuccessResult(ObjectResponseDto result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMsg("정상적으로 처리되었습니다.");
    }

    private void setDuplicatePathErrorResult(ObjectResponseDto result) {
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMsg("이미 존재하는 파일 또는 폴더입니다.");
    }

    private void setCapacityNotEnoughResult(ObjectResponseDto result) {
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMsg("사용 가능한 용량이 충분하지 않습니다.");
    }

    private void setParentPathNotFoundResult(ObjectResponseDto result) {
        result.setSuccess(false);
        result.setCode(CommonResponse.FAIL.getCode());
        result.setMsg("상위 폴더가 존재하지 않습니다.");
    }

}
