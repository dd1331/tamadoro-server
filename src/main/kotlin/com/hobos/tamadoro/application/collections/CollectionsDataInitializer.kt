package com.hobos.tamadoro.application.collections

import com.hobos.tamadoro.domain.collections.BackgroundEntity
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.MusicTrackEntity
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogEntity
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogRepository
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogStageEntity
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogStageRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Profile("!prod")
@Component
class CollectionsDataInitializer(
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val characterRepository: TamagotchiCatalogRepository,
    private val characterStageRepository: TamagotchiCatalogStageRepository,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(CollectionsDataInitializer::class.java)

	@Transactional
	override fun run(args: ApplicationArguments) {
        seedBackgrounds()
        seedMusic()
        seedCharacters()
    }

	private fun seedBackgrounds() {
		val seeds = listOf(
			BackgroundEntity("bg1", "Sunrise", "light", false, "https://picsum.photos/600/600"),
			BackgroundEntity("bg2", "Forest", "nature", true, "https://picsum.photos/800/600"),
			BackgroundEntity("bg3", "Sunrise2", "light", false, "https://picsum.photos/200"),
			BackgroundEntity("bg4", "Forest2", "nature", true, "https://picsum.photos/400"),
		)
		val toInsert = seeds.filter { !backgroundRepository.existsById(it.id) }
		if (toInsert.isNotEmpty()) {
			backgroundRepository.saveAll(toInsert)
			log.info("Seeded backgrounds: ${toInsert.size}")
		}
	}

	private fun seedMusic() {
		val seeds = listOf(
			MusicTrackEntity(
				id = "m1",
				title = "Rain",
				theme = "rain",
				isPremium = false,
				url = "https://picsum.photos/600/600",
				resource = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
			),
			MusicTrackEntity(
				id = "m2",
				title = "Focus Tones2",
				theme = "focus_tones",
				isPremium = true,
				url = "https://picsum.photos/800/600",
				resource = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3",
			),
			MusicTrackEntity(
				id = "m3",
				title = "Focus Tones3",
				theme = "focus_tones",
				isPremium = false,
				url = "https://picsum.photos/800/600",
				resource = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
			),
			MusicTrackEntity(
				id = "m4",
				title = "Focus Tones4",
				theme = "focus_tones",
				isPremium = true,
				url = "https://picsum.photos/800/600",
				resource = "https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3",
			),
		)
		val toInsert = seeds.filter { !musicTrackRepository.existsById(it.id) }
		if (toInsert.isNotEmpty()) {
			musicTrackRepository.saveAll(toInsert)
			log.info("Seeded music tracks: ${toInsert.size}")
		}
	}

	private fun seedCharacters() {
		val pippo = characterRepository.findById("char_001").orElseGet {
			characterRepository.save(
				TamagotchiCatalogEntity(
					id = "char_001",
					title = "Pippo",
					theme = "cat",
					isPremium = false,
					happiness = 80,
					hunger = 20,
					energy = 90,
				)
			)
		}
		val drogo = characterRepository.findById("char_002").orElseGet {
			characterRepository.save(
				TamagotchiCatalogEntity(
					id = "char_002",
					title = "Drogo",
					theme = "dragon",
					isPremium = false,
					happiness = 95,
					hunger = 10,
					energy = 85,
				)
			)
		}
		val drogo2 = characterRepository.findById("char_003").orElseGet {
			characterRepository.save(
				TamagotchiCatalogEntity(
					id = "char_0022",
					title = "Drogo2",
					theme = "dragon2",
					isPremium = true,
					happiness = 95,
					hunger = 10,
					energy = 85,
				)
			)
		}

		val stageSeeds = listOf(
			TamagotchiCatalogStageEntity("${'$'}{pippo.id}_egg", pippo, com.hobos.tamadoro.domain.collections.StageName.EGG, 0, 10, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamagotchiCatalogStageEntity("${'$'}{pippo.id}_baby", pippo, com.hobos.tamadoro.domain.collections.StageName.BABY, 0, 50, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamagotchiCatalogStageEntity("${'$'}{pippo.id}_child", pippo, com.hobos.tamadoro.domain.collections.StageName.CHILD, 0, 150, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamagotchiCatalogStageEntity("${'$'}{pippo.id}_teen", pippo, com.hobos.tamadoro.domain.collections.StageName.TEEN, 0, 300, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamagotchiCatalogStageEntity("${'$'}{pippo.id}_adult", pippo, com.hobos.tamadoro.domain.collections.StageName.ADULT, 0, 500, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),

			TamagotchiCatalogStageEntity("${'$'}{drogo.id}_egg", drogo, com.hobos.tamadoro.domain.collections.StageName.EGG, 0, 20, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo.id}_baby", drogo, com.hobos.tamadoro.domain.collections.StageName.BABY, 0, 80, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo.id}_child", drogo, com.hobos.tamadoro.domain.collections.StageName.CHILD, 0, 200, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo.id}_teen", drogo, com.hobos.tamadoro.domain.collections.StageName.TEEN, 0, 450, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo.id}_adult", drogo, com.hobos.tamadoro.domain.collections.StageName.ADULT, 0, 700, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),

			TamagotchiCatalogStageEntity("${'$'}{drogo2.id}_egg", drogo2, com.hobos.tamadoro.domain.collections.StageName.EGG, 0, 20, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo2.id}_baby", drogo2, com.hobos.tamadoro.domain.collections.StageName.BABY, 0, 80, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo2.id}_child", drogo2, com.hobos.tamadoro.domain.collections.StageName.CHILD, 0, 200, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo2.id}_teen", drogo2, com.hobos.tamadoro.domain.collections.StageName.TEEN, 0, 450, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamagotchiCatalogStageEntity("${'$'}{drogo2.id}_adult", drogo2, com.hobos.tamadoro.domain.collections.StageName.ADULT, 0, 700, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
		)
		val stageToInsert = stageSeeds.filter { !characterStageRepository.existsById(it.id) }
		if (stageToInsert.isNotEmpty()) {
			characterStageRepository.saveAll(stageToInsert)
			log.info("Seeded character stages: ${stageToInsert.size}")
		}
	}
}
