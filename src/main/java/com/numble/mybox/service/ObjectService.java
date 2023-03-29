package com.numble.mybox.service;

import com.numble.mybox.data.dto.FileRequestDto;
import com.numble.mybox.data.dto.ObjectRequestDto;
import com.numble.mybox.data.entity.Object;
import java.io.IOException;
import java.util.List;

public interface ObjectService {

    public List<Object> getObjects(String bucketName, String parentFullName);

    public Object createFolder(ObjectRequestDto objectRequestDto);

    public Object createFile(FileRequestDto fileRequestDto) throws IOException;

}
