package com.hobos.tamadoro.domain.tamas

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MusicTrackRepository : JpaRepository<MusicTrackEntity, Long> {
    fun findByIsPremium(isPremium: Boolean): List<MusicTrackEntity>
}
