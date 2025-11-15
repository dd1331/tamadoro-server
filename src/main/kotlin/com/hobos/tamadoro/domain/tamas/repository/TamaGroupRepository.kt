package com.hobos.tamadoro.domain.tamas.repository

import com.hobos.tamadoro.domain.tamas.entity.TamaGroup
import org.springframework.data.jpa.repository.JpaRepository

interface TamaGroupRepository: JpaRepository<TamaGroup, Long> {
}