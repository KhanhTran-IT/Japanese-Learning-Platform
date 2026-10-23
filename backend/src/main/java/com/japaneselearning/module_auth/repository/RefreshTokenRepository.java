package com.japaneselearning.module_auth.repository;

import com.japaneselearning.module_auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId AND (r.revoked = false OR r.revoked IS NULL)")
    int revokeAllByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now OR r.revoked = true")
    int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);

    List<RefreshToken> findAllByUserId(Long userId);

    long countByUserIdAndRevokedFalseAndExpiredAtAfter(Long userId, LocalDateTime now);
}

