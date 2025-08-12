package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamagotchiCatalogStageRepository : JpaRepository<TamagotchiCatalogStageEntity, String> {
    fun findByTamagotchi_Id(tamagotchiId: String): List<TamagotchiCatalogStageEntity>
}


