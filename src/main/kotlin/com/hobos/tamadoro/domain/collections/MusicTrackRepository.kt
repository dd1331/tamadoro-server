package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MusicTrackRepository : JpaRepository<MusicTrackEntity, Long> {
    fun findByIsPremium(isPremium: Boolean): List<MusicTrackEntity>
}
