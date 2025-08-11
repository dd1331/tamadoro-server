package com.hobos.tamadoro.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for User entity.
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    /**
     * Find a user by email.
     */

    /**
     * Check if a user exists with the given email.
     */


    /**
     * Find a user by providerId.
     */
    fun findByProviderId(providerId: String): Optional<User>
}