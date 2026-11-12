package com.japaneselearning.common.security;

import com.japaneselearning.module_user.enums.UserStatus;

/**
 * Service for managing user token versions and fast revocation lookups
 * without hitting the primary database on every HTTP request.
 */
public interface TokenRevocationService {

    /**
     * Checks if the access token with the given user ID, token version, and email is still valid.
     *
     * @param userId       The user's ID
     * @param tokenVersion The version embedded in the access token
     * @param email        The user's email
     * @return true if valid, false if revoked, locked, deleted, or obsolete version
     */
    boolean isTokenActive(Long userId, Integer tokenVersion, String email);

    /**
     * Invalidates all active access tokens and cached auth state for a user immediately.
     *
     * @param userId The ID of the user whose tokens should be revoked
     */
    void revokeUserTokens(Long userId);

    /**
     * Updates or evicts the cached user security state upon role, status, or deletion changes.
     *
     * @param userId The ID of the user
     */
    void evictUserSecurityState(Long userId);
}
