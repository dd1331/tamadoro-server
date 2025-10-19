package com.hobos.tamadoro.domain.tamas

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BackgroundRepository : JpaRepository<BackgroundEntity, Long> {
    fun findByUrl(url: String): Optional<BackgroundEntity>
    fun findByIsPremiumAndIsCustom(isPremium: Boolean, isCustom: Boolean): List<BackgroundEntity>
}
