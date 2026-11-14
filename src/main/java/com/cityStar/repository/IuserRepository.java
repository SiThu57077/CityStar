package com.cityStar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cityStar.model.User;

@Repository
public interface IuserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email); 
} 
