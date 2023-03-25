package com.numble.mybox.service;

import com.numble.mybox.data.dto.BucketResponseDto;
import com.numble.mybox.data.dto.ObjectResponseDto;
import com.numble.mybox.data.entity.Object;
import java.util.List;

public interface ObjectService {

    public List<Object> getRootObject(String bucketName);

}
