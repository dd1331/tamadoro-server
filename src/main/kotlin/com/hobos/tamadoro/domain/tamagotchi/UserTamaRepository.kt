package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.tamas.UserTama
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

    fun findOneByUserIdAndIsActiveTrue(userId: UUID): UserTama?


    @Query("""
        SELECT ut FROM UserTama ut
        JOIN fetch ut.tama t
        order by ut.experience DESC
    """)
    fun findAllWithTamaOrderByExperienceDesc(pageable: Pageable): Page<UserTama>

    



    /**
     * Count tamas with experience greater than the given value.
     */
    fun countByExperienceGreaterThan(experience: Int): Long
}
