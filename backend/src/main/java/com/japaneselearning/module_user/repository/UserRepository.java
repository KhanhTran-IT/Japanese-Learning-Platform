package com.japaneselearning.module_user.repository;

import com.japaneselearning.module_user.entity.User;
import com.japaneselearning.module_user.enums.UserStatus;
import com.japaneselearning.module_user.enums.RoleName;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roles"})
    List<User> findTop5ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u LEFT JOIN u.roles r " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:role IS NULL OR r.name = :role)")
    Page<User> findUsersByCriteria(@Param("keyword") String keyword, 
                                   @Param("status") UserStatus status, 
                                   @Param("role") RoleName role, 
                                   Pageable pageable);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findById(Long id);
}
