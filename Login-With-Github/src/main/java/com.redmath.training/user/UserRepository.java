package com.redmath.training.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Page<User> findByNameLike(String name, Pageable pageable);
    Optional<User> findByName (String name);

    Page<User> findByRolesLike(String role, Pageable pageable);

}
