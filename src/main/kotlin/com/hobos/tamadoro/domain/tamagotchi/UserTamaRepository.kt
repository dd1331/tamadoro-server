package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.collections.UserTama
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for Tama entity.
 */
@Repository
interface UserTamaRepository : JpaRepository<UserTama, Long> {
    /**
     * Find all tamas for a user.
     */
    fun findByUserId(userId: UUID): List<UserTama>

    fun existsByUser_IdAndId(userId: UUID, itemId: Long): Boolean

    fun findByUserIdAndIsActiveTrue(userId: UUID): List<UserTama>
    fun findOneByUserIdAndIsActiveTrue(userId: UUID): UserTama?

    @Query("""
        SELECT ut FROM UserTama ut
        JOIN fetch ut.tama t
        order by ut.experience DESC
    """)
    fun findAllWithTamaOrderByCreatedAtDesc(): List<UserTama>

    @Query("""
        SELECT ut FROM UserTama ut
        JOIN fetch ut.tama t
        order by ut.experience DESC
    """)
    fun findAllWithTamaOrderByCreatedAtDesc(pageable: Pageable): Page<UserTama>

    
    /**
     * Count the number of tamas a user has.
     */
    fun countByUserId(userId: UUID): Long
    

    /**
     * Find the highest level tama for a user.
     */
    @Query("SELECT t FROM UserTama t WHERE t.user.id = :userId")
    fun findHighestLevelTamaByUserId(userId: UUID): UserTama?
    
    /**
     * Find unhealthy tamas for a user (low happiness, high hunger, or low energy).
     */
    @Query("SELECT t FROM UserTama t WHERE t.user.id = :userId")
    fun findUnhealthyTamasByUserId(userId: UUID): List<UserTama>
    fun findByUserIdAndId(user_id: UUID, id: Long): List<UserTama>
    
    /**
     * Count tamas with experience greater than the given value.
     */
    fun countByExperienceGreaterThan(experience: Int): Long
}
