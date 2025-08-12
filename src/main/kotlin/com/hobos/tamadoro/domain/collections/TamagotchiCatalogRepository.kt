package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TamagotchiCatalogRepository : JpaRepository<TamagotchiCatalogEntity, String>


