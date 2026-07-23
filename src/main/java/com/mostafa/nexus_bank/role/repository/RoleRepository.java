package com.mostafa.nexus_bank.role.repository;

import com.mostafa.nexus_bank.common.enums.RoleType;
import com.mostafa.nexus_bank.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleType name);

}
