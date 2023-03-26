package com.numble.mybox.service;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import java.io.IOException;
import java.util.List;

public interface ObjectService {

    public List<Object> getRootObject(String bucketName);

    public Object createFolder(ObjectRequestDto objectRequestDto);

    public Object createFile(FileRequestDto fileRequestDto) throws IOException;

}
