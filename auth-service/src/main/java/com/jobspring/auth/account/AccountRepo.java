package com.jobspring.auth.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    @Query("""
                SELECT u FROM Account u
                WHERE u.role <> 2
                  AND (:email IS NULL OR u.email LIKE %:email%)
                  AND (:fullName IS NULL OR u.fullName LIKE %:fullName%)
                  AND (:phone IS NULL OR u.phone LIKE %:phone%)
                  AND (:id IS NULL OR u.id = :id)
            """)
    Page<Account> searchUsers(@Param("email") String email,
                           @Param("fullName") String fullName,
                           @Param("phone") String phone,
                           @Param("id") Long id,
                           Pageable pageable);

}