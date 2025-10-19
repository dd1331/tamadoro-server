package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.tamas.UserTama
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
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

    @Query(
        value = """
            SELECT tg.group.id as groupId,
                   tg.group.name as groupName,
                   tg.group.avatar as avatar,
                   tg.group.background as background,
                   SUM(ut.experience) as totalExperience,
                   COUNT(ut.id) as tamaCount
            FROM UserTama ut
            JOIN TamaGroup tg ON tg.tama = ut
            GROUP BY tg.group.id, tg.group.name, tg.group.avatar, tg.group.background
            ORDER BY SUM(ut.experience) DESC
        """,
        countQuery = """
            SELECT COUNT(DISTINCT tg.group.id)
            FROM UserTama ut
            JOIN TamaGroup tg ON tg.tama = ut
        """
    )
    fun findGroupRanking(pageable: Pageable): Page<GroupRankingProjection>



    @Query("""
        SELECT tg.group.id FROM UserTama ut
        JOIN TamaGroup tg ON tg.tama = ut
        WHERE ut.id = :userTamaId
    """)
    fun findGroupIdByUserTamaId(userTamaId: Long): Long?

    /**
     * Count tamas with experience greater than the given value.
     */
    fun countByExperienceGreaterThan(experience: Int): Long
    fun findOneById(tamaId: Long): Optional<UserTama>
}

interface GroupRankingProjection {
    val groupId: Long
    val groupName: String
    val avatar: String?
    val background: String?
    val totalExperience: Long
    val tamaCount: Long
}
