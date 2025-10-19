package com.hobos.tamadoro.domain.inventory

import com.hobos.tamadoro.domain.tamas.UserTama
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
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

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
    
    // TODO: ownership 활용
    fun changeActiveTama(tama: UserTama?) {
        // Deactivate the current active tama if there is one
//        activeTama?.let { it.isActive = false }
        
        // Set the new active tama
//        this.activeTama = tama
        
        // Activate the new tama if it's not null
//        tama?.let { it.isActive = true }
        
        updatedAt = LocalDateTime.now()
    }
    

}