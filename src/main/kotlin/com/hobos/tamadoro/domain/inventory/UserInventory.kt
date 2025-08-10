package com.hobos.tamadoro.domain.inventory

import com.hobos.tamadoro.domain.tamagotchi.Tamagotchi
import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a user's inventory.
 */
@Entity
@Table(name = "user_inventories")
class UserInventory(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "coins", nullable = false)
    var coins: Int = 100, // Start with 100 coins
    
    @Column(name = "gems", nullable = false)
    var gems: Int = 10, // Start with 10 gems
    
    @OneToOne
    @JoinColumn(name = "active_tamagotchi_id")
    var activeTamagotchi: Tamagotchi? = null,
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Adds coins to the user's inventory.
     */
    fun addCoins(amount: Int) {
        if (amount > 0) {
            coins += amount
            updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * Removes coins from the user's inventory.
     * Returns true if the user had enough coins, false otherwise.
     */
    fun removeCoins(amount: Int): Boolean {
        if (amount <= 0) return true
        
        return if (coins >= amount) {
            coins -= amount
            updatedAt = LocalDateTime.now()
            true
        } else {
            false
        }
    }
    
    /**
     * Adds gems to the user's inventory.
     */
    fun addGems(amount: Int) {
        if (amount > 0) {
            gems += amount
            updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * Removes gems from the user's inventory.
     * Returns true if the user had enough gems, false otherwise.
     */
    fun removeGems(amount: Int): Boolean {
        if (amount <= 0) return true
        
        return if (gems >= amount) {
            gems -= amount
            updatedAt = LocalDateTime.now()
            true
        } else {
            false
        }
    }
    
    /**
     * Sets the active tamagotchi.
     */
    fun changeActiveTamagotchi(tamagotchi: Tamagotchi?) {
        // Deactivate the current active tamagotchi if there is one
        activeTamagotchi?.let { it.isActive = false }
        
        // Set the new active tamagotchi
        this.activeTamagotchi = tamagotchi
        
        // Activate the new tamagotchi if it's not null
        tamagotchi?.let { it.isActive = true }
        
        updatedAt = LocalDateTime.now()
    }
    
    /**
     * Checks if the user has enough coins.
     */
    fun hasEnoughCoins(amount: Int): Boolean {
        return coins >= amount
    }
    
    /**
     * Checks if the user has enough gems.
     */
    fun hasEnoughGems(amount: Int): Boolean {
        return gems >= amount
    }
}