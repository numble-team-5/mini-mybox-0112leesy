package com.numble.mybox.data.dto;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String bucket;
    private Double remain;

}
