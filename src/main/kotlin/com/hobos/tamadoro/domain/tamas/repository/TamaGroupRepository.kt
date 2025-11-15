package com.hobos.tamadoro.domain.tamas.repository

import com.hobos.tamadoro.domain.tamas.entity.TamaGroup
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TamaGroupRepository: JpaRepository<TamaGroup, Long> {
    fun findOneByTamaId(id: Long): Optional<TamaGroup>
    fun findOneByTamaIdAndGroupId(tamaId: Long, groupId: Long): Optional<TamaGroup>
}