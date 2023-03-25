package com.numble.mybox.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class Object {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String fullName;

    @Column
    private String parentFullName;

    @Column
    private String name;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private Double size;

    @Column
    private Boolean isFolder;

}
