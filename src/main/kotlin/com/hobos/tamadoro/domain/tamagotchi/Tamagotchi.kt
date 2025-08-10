package com.hobos.tamadoro.domain.tamagotchi

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a tamagotchi virtual pet.
 */
@Entity
@Table(name = "tamagotchis")
class Tamagotchi(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "name", nullable = false)
    var name: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TamagotchiType,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    val rarity: TamagotchiRarity,
    
    @Column(name = "level", nullable = false)
    var level: Int = 1,
    
    @Column(name = "experience", nullable = false)
    var experience: Int = 0,
    
    @Column(name = "max_experience", nullable = false)
    var maxExperience: Int = 100,
    
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = false,
    
    @Column(name = "acquired_at", nullable = false)
    val acquiredAt: LocalDateTime = LocalDateTime.now(),
    
    @Enumerated(EnumType.STRING)
    @Column(name = "growth_stage", nullable = false)
    var growthStage: TamagotchiGrowthStage = TamagotchiGrowthStage.EGG,
    
    @Column(name = "happiness", nullable = false)
    var happiness: Int = 80,
    
    @Column(name = "hunger", nullable = false)
    var hunger: Int = 60,
    
    @Column(name = "energy", nullable = false)
    var energy: Int = 90,
    
    @Column(name = "last_interaction", nullable = false)
    var lastInteraction: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Adds experience to the tamagotchi and levels up if necessary.
     */
    fun addExperience(amount: Int) {
        experience += amount
        lastInteraction = LocalDateTime.now()
        
        while (experience >= maxExperience) {
            levelUp()
        }
    }
    
    /**
     * Levels up the tamagotchi.
     */
    private fun levelUp() {
        level++
        experience -= maxExperience
        maxExperience = (maxExperience * 1.5).toInt()
        
        // Check if growth stage should change
        growthStage = when {
            level >= 20 -> TamagotchiGrowthStage.ADULT
            level >= 10 -> TamagotchiGrowthStage.TEEN
            level >= 5 -> TamagotchiGrowthStage.CHILD
            level >= 2 -> TamagotchiGrowthStage.BABY
            else -> TamagotchiGrowthStage.EGG
        }
    }
    
    /**
     * Feeds the tamagotchi to reduce hunger.
     */
    fun feed(amount: Int = 20) {
        hunger = (hunger - amount).coerceIn(0, 100)
        happiness = (happiness + 5).coerceAtMost(100)
        lastInteraction = LocalDateTime.now()
    }
    
    /**
     * Plays with the tamagotchi to increase happiness.
     */
    fun play(amount: Int = 15) {
        happiness = (happiness + amount).coerceAtMost(100)
        energy = (energy - 10).coerceAtLeast(0)
        hunger = (hunger + 5).coerceAtMost(100)
        lastInteraction = LocalDateTime.now()
        
        // Add some experience when playing
        addExperience(5)
    }
    
    /**
     * Lets the tamagotchi rest to restore energy.
     */
    fun rest(amount: Int = 30) {
        energy = (energy + amount).coerceAtMost(100)
        lastInteraction = LocalDateTime.now()
    }
    
    /**
     * Updates the tamagotchi's status based on time passed.
     */
    fun updateStatus() {
        val hoursPassed = java.time.Duration.between(lastInteraction, LocalDateTime.now()).toHours()
        
        if (hoursPassed > 0) {
            // Decrease stats based on time passed
            hunger = (hunger + hoursPassed * 2).coerceAtMost(100).toInt()
            happiness = (happiness - hoursPassed).coerceAtLeast(0).toInt()
            energy = (energy - hoursPassed / 2).coerceAtLeast(0).toInt()
        }
    }
    
    /**
     * Checks if the tamagotchi is healthy.
     */
    fun isHealthy(): Boolean {
        return happiness > 30 && hunger < 80 && energy > 20
    }
    
    /**
     * Calculates the overall well-being score of the tamagotchi.
     */
    fun calculateWellBeingScore(): Int {
        return ((happiness * 0.4) + ((100 - hunger) * 0.3) + (energy * 0.3)).toInt()
    }
}