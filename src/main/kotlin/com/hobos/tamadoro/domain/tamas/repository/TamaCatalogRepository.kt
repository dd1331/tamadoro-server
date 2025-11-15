package com.hobos.tamadoro.domain.tamas.repository

import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamaCatalogRepository : JpaRepository<TamaCatalog, Long> {
    fun findByIsPremium(isPremium: Boolean): List<TamaCatalog>
}


