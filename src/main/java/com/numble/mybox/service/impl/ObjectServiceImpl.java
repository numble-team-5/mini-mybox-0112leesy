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
import com.numble.mybox.exception.CapacityNotEnoughException;
import com.numble.mybox.exception.ObjectAlreadyExistsException;
import com.numble.mybox.exception.ObjectNotFoundException;
import com.numble.mybox.service.BucketService;
import com.numble.mybox.service.ObjectService;
import com.numble.mybox.service.StorageService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ObjectServiceImpl implements ObjectService {

    private final StorageService storageService;
    private final BucketService bucketService;
    private final ObjectRepository objectRepository;

    @Autowired
    public ObjectServiceImpl(StorageService storageService, BucketService bucketService,
        ObjectRepository objectRepository) {
        this.storageService = storageService;
        this.bucketService = bucketService;
        this.objectRepository = objectRepository;
    }

    @Override
    public List<Object> getObjects(String bucketName, String parentPath)
        throws ObjectNotFoundException {
        validateParentPath(bucketName, parentPath);
        List<Object> objects = objectRepository.findByBucketNameAndParentPath(bucketName,
            parentPath);
        return objects;
    }

    @Override
    public ObjectResponseDto createFolder(ObjectRequestDto objectRequestDto)
        throws ObjectAlreadyExistsException, ObjectNotFoundException {
        String path = objectRequestDto.getParentPath() + objectRequestDto.getName();
        validatePath(objectRequestDto.getBucketName(), path);
        validateParentPath(objectRequestDto.getBucketName(), objectRequestDto.getParentPath());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(objectRequestDto.getBucketName(),
            path, new ByteArrayInputStream(new byte[0]), objectMetadata);
        storageService.putObject(putObjectRequest, path);

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
        log.info("folder {} created in Bucket {}.", path, objectRequestDto.getBucketName());

        return objectResponseDto;
    }

    @Override
    public ObjectResponseDto createFile(FileRequestDto fileRequestDto)
        throws IOException, ObjectAlreadyExistsException, ObjectNotFoundException {
        MultipartFile multipartFile = fileRequestDto.getMultipartFile();
        String contentType = multipartFile.getContentType();
        String fileName = Normalizer.normalize(multipartFile.getOriginalFilename(),
            Normalizer.Form.NFC);
        String path = fileRequestDto.getParentPath() + fileName;
        validatePath(fileRequestDto.getBucketName(), path);
        validateParentPath(fileRequestDto.getBucketName(), fileRequestDto.getParentPath());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        long fileSize = multipartFile.getSize();
        objectMetadata.setContentLength(fileSize);
        double fileSizeMb = fileSize / 1024.0 / 1024.0;
        objectMetadata.setContentType(contentType);
        validateCapacity(fileRequestDto.getBucketName(), fileSizeMb);

        PutObjectRequest putObjectRequest = new PutObjectRequest(fileRequestDto.getBucketName(),
            path, multipartFile.getInputStream(), objectMetadata);
        storageService.putObject(putObjectRequest, path);
        bucketService.decreaseCapacity(fileRequestDto.getBucketName(), fileSizeMb);

        Object newFile = Object.builder()
            .bucketName(fileRequestDto.getBucketName())
            .name(fileName)
            .size(fileSizeMb)
            .path(path)
            .parentPath(fileRequestDto.getParentPath())
            .isFolder(false)
            .build();

        ObjectResponseDto objectResponseDto = objectToObjectResponseDto(
            objectRepository.save(newFile));
        log.info("Object {} (size: {}Mb)has been created in Bucket {}.", fileName, fileSizeMb,
            fileRequestDto.getBucketName());
        return objectResponseDto;
    }

    @Override
    public boolean deleteFolder(ObjectRequestDto objectRequestDto) throws ObjectNotFoundException {
        String folderPath = objectRequestDto.getParentPath() + objectRequestDto.getName();
        validateParentPath(objectRequestDto.getBucketName(), folderPath);
        storageService.deleteFolder(objectRequestDto.getBucketName(), folderPath);
        Object folder = objectRepository.findByBucketNameAndPath(objectRequestDto.getBucketName(),
            folderPath);
        bucketService.decreaseCapacity(objectRequestDto.getBucketName(), folder.getSize());
        objectRepository.deleteByBucketNameAndParentPath(objectRequestDto.getBucketName(),
            folderPath);
        objectRepository.deleteByBucketNameAndPath(objectRequestDto.getBucketName(), folderPath);
        log.info("Folder {} (size: {}Mb)has been deleted in Bucket {}.", folderPath,
            folder.getSize(),
            objectRequestDto.getBucketName());
        return true;
    }

    @Override
    public boolean deleteFile(ObjectRequestDto objectRequestDto) {
        String filePath = objectRequestDto.getParentPath() + objectRequestDto.getName();
        storageService.deleteObject(objectRequestDto.getBucketName(), filePath);
        Object file = objectRepository.findByBucketNameAndPath(objectRequestDto.getBucketName(),
            filePath);
        bucketService.decreaseCapacity(objectRequestDto.getBucketName(), file.getSize());
        objectRepository.deleteByBucketNameAndPath(objectRequestDto.getBucketName(), filePath);
        log.info("File {} (size: {}Mb)has been deleted in Bucket {}.", filePath, file.getSize(),
            objectRequestDto.getBucketName());
        return true;
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

    private void validatePath(String bucketName, String path) throws ObjectAlreadyExistsException {
        if (doesPathExist(bucketName, path)) {
            throw new ObjectAlreadyExistsException("이미 존재하는 파일 또는 폴더입니다.");
        }
    }

    private void validateParentPath(String bucketName, String parentPath)
        throws ObjectNotFoundException {
        if (!parentPath.isEmpty() && !doesPathExist(bucketName, parentPath)) {
            throw new ObjectNotFoundException("상위 폴더가 존재하지 않습니다.");
        }
    }

    private void validateCapacity(String bucketName, double fileSizeMb)
        throws CapacityNotEnoughException {
        if (!bucketService.isCapacityEnough(bucketName, fileSizeMb)) {
            throw new CapacityNotEnoughException("사용 가능한 용량이 충분하지 않습니다.");
        }
    }

}
