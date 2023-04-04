package com.numble.mybox.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
        List<Object> objects = objectRepository.findByBucketNameAndParentPath(bucketName, parentPath);
        return objects;
    }

    @Override
    public ObjectResponseDto createFolder(ObjectRequestDto objectRequestDto) {
        String path = objectRequestDto.getParentPath() + objectRequestDto.getName();
        if(isDuplicatePath(objectRequestDto.getBucketName(), path)) {
            ObjectResponseDto objectResponseDto = new ObjectResponseDto();
            setDuplicatePathErrorResult(objectResponseDto);
            return objectResponseDto;
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(objectRequestDto.getBucketName(),
            path, new ByteArrayInputStream(new byte[0]), objectMetadata);

        S3PutObject(putObjectRequest, objectRequestDto.getName());

        Object newFolder = Object.builder()
            .bucketName(objectRequestDto.getBucketName())
            .name(objectRequestDto.getName())
            .size(0.0)
            .path(path)
            .parentPath(objectRequestDto.getParentPath())
            .isFolder(true)
            .build();

        ObjectResponseDto objectResponseDto = objectToObjectResponseDto(objectRepository.save(newFolder));
        setSuccessResult(objectResponseDto);
        return objectResponseDto;
    }

    @Override
    public ObjectResponseDto createFile(FileRequestDto fileRequestDto) throws IOException {
        MultipartFile multipartFile = fileRequestDto.getMultipartFile();
        String contentType = multipartFile.getContentType();
        String fileName = Normalizer.normalize(multipartFile.getOriginalFilename(), Normalizer.Form.NFC);
        String path = fileRequestDto.getParentPath() + fileName;

        ObjectResponseDto objectResponseDto = new ObjectResponseDto();
        if(isDuplicatePath(fileRequestDto.getBucketName(), path)) {
            setDuplicatePathErrorResult(objectResponseDto);
            return objectResponseDto;
        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        long fileSize = multipartFile.getSize();
        System.out.format("Object %s has been created.\n", fileName);
        objectMetadata.setContentLength(fileSize);
        double fileSizeMb = fileSize / 1024.0 / 1024.0;
        System.out.format("File size : %.2f mb\n", fileSizeMb);
        objectMetadata.setContentType(contentType);

        if(!bucketService.isCapacityEnough(fileRequestDto.getBucketName(), fileSizeMb)) {
            setCapacityNotEnoughResult(objectResponseDto);
            return objectResponseDto;
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(fileRequestDto.getBucketName(),
            path, multipartFile.getInputStream(), objectMetadata);

        S3PutObject(putObjectRequest, fileName);

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

    private void S3PutObject(PutObjectRequest putObjectRequest, String objectName) {
        try {
            amazonS3.putObject(putObjectRequest);
            System.out.format("Object %s has been created.\n", objectName);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicatePath(String bucketName, String path) {
        List<Object> objectWithPath = objectRepository.findByBucketNameAndPath(bucketName, path);
        if(objectWithPath.size() > 0) {
            return true;
        }
        return false;
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

}
