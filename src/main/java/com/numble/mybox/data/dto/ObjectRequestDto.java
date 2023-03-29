package com.numble.mybox.data.dto;

import javax.persistence.Column;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectRequestDto {

    private String bucketName;
    private String name;
    private String parentPath;
    private Boolean isFolder;

}
