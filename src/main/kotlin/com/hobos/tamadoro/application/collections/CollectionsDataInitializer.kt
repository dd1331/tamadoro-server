package com.hobos.tamadoro.application.collections

import TamaFixtures
import com.hobos.tamadoro.domain.collections.BackgroundEntity
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.MusicTrackEntity
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.StageName
import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.TamaCatalogStageEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogStageRepository
import com.hobos.tamadoro.test.fixtures.BackgroundFixture
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
    private val characterRepository: TamaCatalogRepository,
    private val characterStageRepository: TamaCatalogStageRepository,
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
			BackgroundFixture.create(),
			BackgroundFixture.create()
		)
		val toInsert = seeds.filter { !backgroundRepository.existsById(it.id) }
		if (toInsert.isNotEmpty()) {
			backgroundRepository.saveAll(toInsert)
			log.info("Seeded backgrounds: ${toInsert.size}")
		}
	}

	private fun seedMusic() {
		val seeds = listOf(
			MusicTrackFixtures.create(),
			MusicTrackFixtures.create(),
			MusicTrackFixtures.create(),
			MusicTrackFixtures.create(),

		)
		val toInsert = seeds.filter { !musicTrackRepository.existsById(it.id) }
		if (toInsert.isNotEmpty()) {
			musicTrackRepository.saveAll(toInsert)
			log.info("Seeded music tracks: ${toInsert.size}")
		}
	}

	private fun seedCharacters() {
		val pippo = characterRepository.findById(1L).orElseGet {
			characterRepository.save(
				TamaFixtures.create()
			)
		}
		val drogo = characterRepository.findById(2L).orElseGet {
			characterRepository.save(
				TamaFixtures.create()
			)
		}
		val drogo2 = characterRepository.findById(2L).orElseGet {
			characterRepository.save(
				TamaFixtures.create()
			)
		}

		val stageSeeds = listOf(
			TamaCatalogStageEntity(StageName.EGG, 10, 100, 150,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity(StageName.EGG, 10, 100, 10,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity(StageName.EGG, 10, 100, 50,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity(StageName.EGG, 10, 100, 300,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity(StageName.EGG, 10, 100, 500,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity(StageName.ADULT, 20, 100, 20,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity(StageName.ADULT, 20, 100, 80,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity(StageName.ADULT, 20, 100, 200,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity(StageName.ADULT, 20, 100, 450,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity(StageName.ADULT, 20, 100, 700,  "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
		)
		val stageToInsert = stageSeeds.filter { !characterStageRepository.existsById(it.id) }
		if (stageToInsert.isNotEmpty()) {
			characterStageRepository.saveAll(stageToInsert)
			log.info("Seeded character stages: ${stageToInsert.size}")
		}
	}
}
