package com.example.stockexchange.repository;

import com.example.stockexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(s) from User s JOIN s.authorities a where a.authority = 'ROLE_ADMIN' ")
    long countAdminUser();
}
