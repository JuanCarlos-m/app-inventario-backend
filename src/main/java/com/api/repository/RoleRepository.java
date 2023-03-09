package com.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.model.ERole;
import com.api.model.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {
 Optional<Roles> findByName(ERole name);
}
