package com.numble.mybox.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRequestDto {

    private MultipartFile multipartFile;
    private String bucketName;
    private String parentPath;

}
