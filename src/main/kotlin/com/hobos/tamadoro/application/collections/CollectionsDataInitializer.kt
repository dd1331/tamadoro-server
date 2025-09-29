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
			TamaCatalogStageEntity( pippo, StageName.EGG, 0, 10, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity( pippo, StageName.BABY, 0, 50, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity( pippo, StageName.CHILD, 0, 150, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity( pippo, StageName.TEEN, 0, 300, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity( pippo, StageName.ADULT, 0, 500, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
			TamaCatalogStageEntity( drogo, StageName.EGG, 0, 20, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity( drogo, StageName.BABY, 0, 80, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity( drogo, StageName.CHILD, 0, 200, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity( drogo, StageName.TEEN, 0, 450, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
			TamaCatalogStageEntity( drogo, StageName.ADULT, 0, 700, 1, "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
		)
		val stageToInsert = stageSeeds.filter { !characterStageRepository.existsById(it.id) }
		if (stageToInsert.isNotEmpty()) {
			characterStageRepository.saveAll(stageToInsert)
			log.info("Seeded character stages: ${stageToInsert.size}")
		}
	}
}
