package com.anhub.subscriboholic.repository;

import com.anhub.subscriboholic.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
