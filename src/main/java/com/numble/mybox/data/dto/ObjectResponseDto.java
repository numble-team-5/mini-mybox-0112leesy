package com.numble.mybox.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectResponseDto {

    private String name;
    private String path;
    private String bucketName;
    private String parentPath;
    private Double size;
    private Boolean isFolder;

}
