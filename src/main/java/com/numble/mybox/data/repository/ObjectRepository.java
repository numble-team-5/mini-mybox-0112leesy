package com.numble.mybox.data.repository;

import com.numble.mybox.data.entity.Object;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectRepository extends JpaRepository<Object, Long> {
}
