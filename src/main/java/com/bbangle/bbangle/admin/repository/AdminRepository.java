package com.bbangle.bbangle.admin.repository;

import com.bbangle.bbangle.admin.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAccountId(String accountId);
}
