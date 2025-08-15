package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class TamaTest {
    
    private lateinit var user: User
    private lateinit var tama: Tama
    
    @BeforeEach
    fun setUp() {
        user = User(
            id = UUID.randomUUID(),
            providerId = UUID.randomUUID().toString(),
        )
        
        tama = Tama(
            user = user,
            name = "TestPet",
            type = TamaType.TOMATO,
            rarity = TamaRarity.COMMON
        )
    }
    
    @Test
    fun `should initialize with correct default values`() {
        assertEquals(1, tama.level)
        assertEquals(0, tama.experience)
        assertEquals(100, tama.maxExperience)
        assertEquals(TamaGrowthStage.EGG, tama.growthStage)
        assertEquals(80, tama.happiness)
        assertEquals(60, tama.hunger)
        assertEquals(90, tama.energy)
        assertFalse(tama.isActive)
    }
    
    @Test
    fun `should add experience correctly`() {
        tama.addExperience(50)
        
        assertEquals(50, tama.experience)
        assertEquals(1, tama.level)
        assertEquals(TamaGrowthStage.EGG, tama.growthStage)
    }
    
    @Test
    fun `should level up when experience reaches max experience`() {
        tama.addExperience(100)
        
        assertEquals(0, tama.experience)
        assertEquals(2, tama.level)
        assertEquals(150, tama.maxExperience) // 100 * 1.5 = 150
        assertEquals(TamaGrowthStage.BABY, tama.growthStage)
    }
    
    @Test
    fun `should level up multiple times when adding large amount of experience`() {
        tama.addExperience(300) // Should level up 2 times

        // 실제 로직상: 100 exp -> level 2, 150 exp -> level 3, 남은 50 exp
        assertEquals(3, tama.level)
        assertEquals(50, tama.experience)
        assertEquals(225, tama.maxExperience) // 150 * 1.5 = 225
        assertEquals(TamaGrowthStage.BABY, tama.growthStage)
    }
    
    @Test
    fun `should change growth stage based on level`() {
        // Level up to 2 (BABY)
        tama.addExperience(100)
        assertEquals(TamaGrowthStage.BABY, tama.growthStage)
        
        // Level up to 5 (CHILD)
        tama.addExperience(1000)
        assertTrue(tama.level >= 5)
        assertEquals(TamaGrowthStage.CHILD, tama.growthStage)
    }
    
    @Test
    fun `should feed correctly`() {
        tama.hunger = 80
        tama.feed(30)
        
        assertEquals(50, tama.hunger)
        assertEquals(85, tama.happiness) // Happiness should increase by 5
    }
    
    @Test
    fun `should not allow hunger to go below 0`() {
        tama.hunger = 10
        tama.feed(50)
        
        assertEquals(0, tama.hunger)
    }
    
    @Test
    fun `should play correctly`() {
        tama.happiness = 70
        tama.energy = 80
        tama.hunger = 50
        
        tama.play(20)
        
        assertEquals(90, tama.happiness) // Happiness should increase by 20
        assertEquals(70, tama.energy) // Energy should decrease by 10
        assertEquals(55, tama.hunger) // Hunger should increase by 5
        
        // Should also gain some experience
        assertTrue(tama.experience > 0)
    }
    
    @Test
    fun `should not allow happiness to exceed 100`() {
        tama.happiness = 90
        tama.play(20)
        
        assertEquals(100, tama.happiness)
    }
    
    @Test
    fun `should rest correctly`() {
        tama.energy = 50
        tama.rest(40)
        
        assertEquals(90, tama.energy)
    }
    
    @Test
    fun `should not allow energy to exceed 100`() {
        tama.energy = 80
        tama.rest(30)
        
        assertEquals(100, tama.energy)
    }
    
    @Test
    fun `should update status based on time passed`() {
        // Set last interaction to 5 hours ago
        val fiveHoursAgo = LocalDateTime.now().minusHours(5)
        tama = Tama(
            user = user,
            name = "TestPet",
            type = TamaType.TOMATO,
            rarity = TamaRarity.COMMON,
            lastInteraction = fiveHoursAgo
        )
        
        val initialHappiness = tama.happiness
        val initialHunger = tama.hunger
        val initialEnergy = tama.energy
        
        tama.updateStatus()
        
        // Happiness should decrease by 5 (1 per hour)
        assertEquals(initialHappiness - 5, tama.happiness)
        
        // Hunger should increase by 10 (2 per hour)
        assertEquals(initialHunger + 10, tama.hunger)
        
        // Energy should decrease by 2 (0.5 per hour, rounded to 2)
        assertEquals(initialEnergy - 2, tama.energy)
    }
    
    @Test
    fun `should calculate well-being score correctly`() {
        tama.happiness = 80
        tama.hunger = 40
        tama.energy = 70
        
        // Well-being score = (happiness * 0.4) + ((100 - hunger) * 0.3) + (energy * 0.3)
        // = (80 * 0.4) + ((100 - 40) * 0.3) + (70 * 0.3)
        // = 32 + 18 + 21 = 71
        
        assertEquals(71, tama.calculateWellBeingScore())
    }
    
    @Test
    fun `should determine if tama is healthy`() {
        // Healthy tama
        tama.happiness = 50
        tama.hunger = 60
        tama.energy = 40
        assertTrue(tama.isHealthy())
        
        // Unhealthy due to low happiness
        tama.happiness = 20
        assertFalse(tama.isHealthy())
        
        // Reset happiness, unhealthy due to high hunger
        tama.happiness = 50
        tama.hunger = 90
        assertFalse(tama.isHealthy())
        
        // Reset hunger, unhealthy due to low energy
        tama.hunger = 60
        tama.energy = 10
        assertFalse(tama.isHealthy())
    }
}