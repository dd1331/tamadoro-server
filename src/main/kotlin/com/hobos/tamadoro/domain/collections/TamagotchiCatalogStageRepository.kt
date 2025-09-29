package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamaCatalogStageRepository : JpaRepository<TamaCatalogStageEntity, Long> {
    fun findByTama_Id(tamaId: Long): List<TamaCatalogStageEntity>
}


