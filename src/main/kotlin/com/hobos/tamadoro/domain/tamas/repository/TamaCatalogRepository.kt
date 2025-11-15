package com.hobos.tamadoro.domain.tamas.repository

import com.hobos.tamadoro.domain.tamas.entity.TamaCatalogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamaCatalogRepository : JpaRepository<TamaCatalogEntity, Long> {
    fun findByIsPremium(isPremium: Boolean): List<TamaCatalogEntity>
}


