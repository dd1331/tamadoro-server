package com.hobos.tamadoro.domain.tamagotchi

import com.hobos.tamadoro.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class TamagotchiTest {
    
    private lateinit var user: User
    private lateinit var tamagotchi: Tamagotchi
    
    @BeforeEach
    fun setUp() {
        user = User(
            id = UUID.randomUUID(),
            email = "test@example.com",
            name = "Test User"
        )
        
        tamagotchi = Tamagotchi(
            user = user,
            name = "TestPet",
            type = TamagotchiType.TOMATO,
            rarity = TamagotchiRarity.COMMON
        )
    }
    
    @Test
    fun `should initialize with correct default values`() {
        assertEquals(1, tamagotchi.level)
        assertEquals(0, tamagotchi.experience)
        assertEquals(100, tamagotchi.maxExperience)
        assertEquals(TamagotchiGrowthStage.EGG, tamagotchi.growthStage)
        assertEquals(80, tamagotchi.happiness)
        assertEquals(60, tamagotchi.hunger)
        assertEquals(90, tamagotchi.energy)
        assertFalse(tamagotchi.isActive)
    }
    
    @Test
    fun `should add experience correctly`() {
        tamagotchi.addExperience(50)
        
        assertEquals(50, tamagotchi.experience)
        assertEquals(1, tamagotchi.level)
        assertEquals(TamagotchiGrowthStage.EGG, tamagotchi.growthStage)
    }
    
    @Test
    fun `should level up when experience reaches max experience`() {
        tamagotchi.addExperience(100)
        
        assertEquals(0, tamagotchi.experience)
        assertEquals(2, tamagotchi.level)
        assertEquals(150, tamagotchi.maxExperience) // 100 * 1.5 = 150
        assertEquals(TamagotchiGrowthStage.BABY, tamagotchi.growthStage)
    }
    
    @Test
    fun `should level up multiple times when adding large amount of experience`() {
        tamagotchi.addExperience(300) // Should level up 2 times

        // 실제 로직상: 100 exp -> level 2, 150 exp -> level 3, 남은 50 exp
        assertEquals(3, tamagotchi.level)
        assertEquals(50, tamagotchi.experience)
        assertEquals(225, tamagotchi.maxExperience) // 150 * 1.5 = 225
        assertEquals(TamagotchiGrowthStage.BABY, tamagotchi.growthStage)
    }
    
    @Test
    fun `should change growth stage based on level`() {
        // Level up to 2 (BABY)
        tamagotchi.addExperience(100)
        assertEquals(TamagotchiGrowthStage.BABY, tamagotchi.growthStage)
        
        // Level up to 5 (CHILD)
        tamagotchi.addExperience(1000)
        assertTrue(tamagotchi.level >= 5)
        assertEquals(TamagotchiGrowthStage.CHILD, tamagotchi.growthStage)
    }
    
    @Test
    fun `should feed correctly`() {
        tamagotchi.hunger = 80
        tamagotchi.feed(30)
        
        assertEquals(50, tamagotchi.hunger)
        assertEquals(85, tamagotchi.happiness) // Happiness should increase by 5
    }
    
    @Test
    fun `should not allow hunger to go below 0`() {
        tamagotchi.hunger = 10
        tamagotchi.feed(50)
        
        assertEquals(0, tamagotchi.hunger)
    }
    
    @Test
    fun `should play correctly`() {
        tamagotchi.happiness = 70
        tamagotchi.energy = 80
        tamagotchi.hunger = 50
        
        tamagotchi.play(20)
        
        assertEquals(90, tamagotchi.happiness) // Happiness should increase by 20
        assertEquals(70, tamagotchi.energy) // Energy should decrease by 10
        assertEquals(55, tamagotchi.hunger) // Hunger should increase by 5
        
        // Should also gain some experience
        assertTrue(tamagotchi.experience > 0)
    }
    
    @Test
    fun `should not allow happiness to exceed 100`() {
        tamagotchi.happiness = 90
        tamagotchi.play(20)
        
        assertEquals(100, tamagotchi.happiness)
    }
    
    @Test
    fun `should rest correctly`() {
        tamagotchi.energy = 50
        tamagotchi.rest(40)
        
        assertEquals(90, tamagotchi.energy)
    }
    
    @Test
    fun `should not allow energy to exceed 100`() {
        tamagotchi.energy = 80
        tamagotchi.rest(30)
        
        assertEquals(100, tamagotchi.energy)
    }
    
    @Test
    fun `should update status based on time passed`() {
        // Set last interaction to 5 hours ago
        val fiveHoursAgo = LocalDateTime.now().minusHours(5)
        tamagotchi = Tamagotchi(
            user = user,
            name = "TestPet",
            type = TamagotchiType.TOMATO,
            rarity = TamagotchiRarity.COMMON,
            lastInteraction = fiveHoursAgo
        )
        
        val initialHappiness = tamagotchi.happiness
        val initialHunger = tamagotchi.hunger
        val initialEnergy = tamagotchi.energy
        
        tamagotchi.updateStatus()
        
        // Happiness should decrease by 5 (1 per hour)
        assertEquals(initialHappiness - 5, tamagotchi.happiness)
        
        // Hunger should increase by 10 (2 per hour)
        assertEquals(initialHunger + 10, tamagotchi.hunger)
        
        // Energy should decrease by 2 (0.5 per hour, rounded to 2)
        assertEquals(initialEnergy - 2, tamagotchi.energy)
    }
    
    @Test
    fun `should calculate well-being score correctly`() {
        tamagotchi.happiness = 80
        tamagotchi.hunger = 40
        tamagotchi.energy = 70
        
        // Well-being score = (happiness * 0.4) + ((100 - hunger) * 0.3) + (energy * 0.3)
        // = (80 * 0.4) + ((100 - 40) * 0.3) + (70 * 0.3)
        // = 32 + 18 + 21 = 71
        
        assertEquals(71, tamagotchi.calculateWellBeingScore())
    }
    
    @Test
    fun `should determine if tamagotchi is healthy`() {
        // Healthy tamagotchi
        tamagotchi.happiness = 50
        tamagotchi.hunger = 60
        tamagotchi.energy = 40
        assertTrue(tamagotchi.isHealthy())
        
        // Unhealthy due to low happiness
        tamagotchi.happiness = 20
        assertFalse(tamagotchi.isHealthy())
        
        // Reset happiness, unhealthy due to high hunger
        tamagotchi.happiness = 50
        tamagotchi.hunger = 90
        assertFalse(tamagotchi.isHealthy())
        
        // Reset hunger, unhealthy due to low energy
        tamagotchi.hunger = 60
        tamagotchi.energy = 10
        assertFalse(tamagotchi.isHealthy())
    }
}