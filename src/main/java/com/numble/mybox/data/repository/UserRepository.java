package com.numble.mybox.data.repository;

import com.numble.mybox.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
