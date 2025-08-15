package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamaCatalogStageRepository : JpaRepository<TamaCatalogStageEntity, String> {
    fun findByTama_Id(tamaId: String): List<TamaCatalogStageEntity>
}


