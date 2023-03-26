package com.numble.mybox.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BucketResponseDto {

    private String username;
    private String bucketName;
    private Double remain;

}
