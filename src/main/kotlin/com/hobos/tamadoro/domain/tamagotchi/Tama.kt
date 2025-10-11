package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.util.UUID

/**
 * Entity representing a tama virtual pet.
 */
@Entity
@Table(name = "tamas")
class Tama(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "tama_catalog_id", nullable = false)
    val tamaCatalogEntity: TamaCatalogEntity,





    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "is_active")
    var isActive: Boolean = false,
)
{

    @Column(name = "happiness")
    final var happiness: Int = 100
        private set

    @Column(name = "energy")
    final var energy: Int = 100
        private set


    @Column(name = "experience")
    final var experience: Int = 0
        private set

    @Column(name="hunger")
    final var hunger: Int = 100
        private set

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

