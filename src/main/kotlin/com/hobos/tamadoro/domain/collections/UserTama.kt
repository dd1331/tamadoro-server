package com.hobos.tamadoro.domain.collections

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

// TODO: 카테고리별로 테이블 구성
@Entity
@Table(
    name = "user_collection_ownerships",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "category", "item_id"])],
    indexes = [Index(name = "idx_user_category", columnList = "user_id,category")]
)
class UserTama(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tama_id", nullable = false)
    val tama: TamaCatalogEntity?,


    @Column(name = " name", nullable = true)
    var name: String = "",

    @Column(name = "is_active")
    var isActive: Boolean = false,
) {

    @Column(name = "happiness")
    final var happiness: Int = 100
        private set

    @Column(name = "energy")
    final var energy: Int = 100
        private set


    @Column(name = "experience")
    final var experience: Int = 0
        private set

    @Column(name = "hunger")
    final var hunger: Int = 100
        private set

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()


    fun addExperience(amount: Int) {
        require(amount > 0)
        this.experience += amount
    }

    fun feed(amount: Int) {
        require(amount > 0)
        when {
            this.hunger - amount <= 0 -> this.hunger = 0
            else -> this.hunger -= amount
        }
    }

    fun play(amount: Int) {
        // TODO: define what and amount to deduct or induct.
        //  set validation
        require(amount > 0)
        this.energy -= amount
        this.happiness += amount
        this.experience += amount
    }


}
