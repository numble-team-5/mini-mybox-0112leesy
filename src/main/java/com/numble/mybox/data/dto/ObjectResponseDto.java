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
    private String fullName;
    private String bucketName;
    private String parentFullName;
    private Double size;
    private Boolean isFolder;

}
